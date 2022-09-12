@file:Suppress("DuplicatedCode")

package discord.route

import core.assets.VOID_MESSAGE_REF
import core.database.repositories.AnnounceRepository
import core.interact.commands.AnnounceCommand
import core.interact.commands.Command
import core.interact.commands.ResponseFlag
import core.interact.commands.UpdateProfileCommand
import core.interact.i18n.LanguageContainer
import core.interact.message.DiPublisherSet
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import discord.assets.*
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.*
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import utils.structs.*
import java.util.concurrent.TimeUnit

private fun buildPermissionNode(context: InteractionContext<*>, parsableCommand: ParsableCommand, channel: TextChannel, jdaUser: User): Either<ParsableCommand, DiscordParseFailure> =
    GuildManager.permissionDependedRun(
        channel, Permission.MESSAGE_SEND,
        onMissed = { Either.Right(
            DiscordParseFailure(parsableCommand.name, "message permission not granted in $channel", context.guild, context.user) { _, _, container ->
                IO {
                    jdaUser.openPrivateChannel()
                        .flatMap { privateChannel ->
                            DiscordMessageProducer.sendPermissionNotGrantedEmbed(
                                publisher = { msg -> privateChannel.sendMessage(msg) },
                                container = container,
                                channelName = channel.name
                            )
                        }
                        .delay(1, TimeUnit.MINUTES)
                        .flatMap(Message::delete)
                        .queue()

                    emptyList()
                }
            }
        ) },
        onGranted = { Either.Left(parsableCommand) }
    )

private fun <T : Event> buildAnnounceNode(context: InteractionContext<T>, command: Command): Command =
    when {
        (context.user.announceId ?: -1) < (AnnounceRepository.getLatestAnnounceId(context.bot.dbConnection) ?: -1) ->
            AnnounceCommand(command)
        else -> command
    }

private fun <T : Event> buildUpdateProfileNode(context: InteractionContext<T>, command: Command, jdaUser: User): Command {
    val user = jdaUser.extractProfile(uid = context.user.id, announceId = context.user.announceId)
    val guild = context.jdaGuild.extractProfile(uid = context.guild.id)

    return when {
        user != context.user ->
            UpdateProfileCommand(command, Either.Left(user))
        guild != context.guild ->
            UpdateProfileCommand(command, Either.Right(guild))
        else -> command
    }
}

private fun matchCommand(command: String, container: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        "help" -> Option(HelpCommandParser)
        container.helpCommand() -> Option(HelpCommandParser)
        container.settingsCommand() -> Option(SettingsCommandParser)
        container.startCommand() -> Option(StartCommandParser)
        "s" -> Option(SetCommandParser)
        container.resignCommand() -> Option(ResignCommandParser)
        container.languageCommand() -> Option(LangCommandParser)
        container.styleCommand() -> Option(StyleCommandParser)
        container.rankCommand() -> Option(RankCommandParser)
        container.ratingCommand() -> Option(RatingCommandParser)
        "debug" -> Option(DebugCommandParser)
        else -> Option.Empty
    }

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<InteractionReport> =
    mono {
        matchCommand(context.event.name, context.config.language.container)
            .map { parsable ->
                buildPermissionNode(context, parsable, context.event.channel.asTextChannel(), context.event.user)
                    .flatMapLeft {
                        parsable.parseSlash(context)
                    }
                    .mapLeft { command ->
                        buildAnnounceNode(context, command)
                    }
                    .mapLeft { command ->
                        buildUpdateProfileNode(context, command, context.event.user)
                    }
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext { parsed ->
            parsed.onLeft {
                if (it.responseFlag is ResponseFlag.Defer)
                    context.event.deferReply().queue()
            }
        }
        .flatMap { parsed -> mono {
            parsed.fold(
                onLeft = { command ->
                    command.execute(
                        bot = context.bot,
                        config = context.config,
                        guild = context.guild,
                        user = context.user,
                        producer = DiscordMessageProducer,
                        messageRef = VOID_MESSAGE_REF,
                        publishers = when (command.responseFlag) {
                            is ResponseFlag.Defer -> DiPublisherSet(
                                plain = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) },
                                windowed = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg).setEphemeral(true)) },
                            )
                            else -> DiPublisherSet(
                                plain = TransMessagePublisher(
                                    head = { msg -> ReplyActionAdaptor(context.event.reply(msg)) },
                                    tail = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) }
                                ),
                                windowed = { msg -> ReplyActionAdaptor(context.event.reply(msg).setEphemeral(true)) },
                            )
                        }
                    )
                },
                onRight = { parseFailure ->
                    parseFailure.notice(
                        config = context.config,
                        producer = DiscordMessageProducer,
                        publisher = TransMessagePublisher(
                            head = { msg -> ReplyActionAdaptor(context.event.reply(msg).setEphemeral(true)) },
                            tail = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg).setEphemeral(true)) }
                        )
                    )
                }
            ).fold(
                onSuccess = { (io, report) ->
                    export(context, io, null)
                    report
                },
                onFailure = { throwable ->
                    ErrorReport(throwable, context.guild)
                }
            ).apply {
                interactionSource = getEventAbbreviation(context.event::class)
                emittedTime = context.emittedTime
            }
        } }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<InteractionReport> {
    val messageRaw = context.event.message.contentRaw

    val payload = when {
        messageRaw.startsWith(COMMAND_PREFIX) -> messageRaw.drop(1).split(" ")
        else -> {
            // drop <@000000000000000000>
            val payload = messageRaw.drop(21).trimStart().split(" ")

            when {
                payload.first().isEmpty() -> listOf("help")
                else -> payload
            }
        }
    }

    return mono {
        matchCommand(command = payload.first(), container = context.config.language.container)
            .map { parsable ->
                buildPermissionNode(context, parsable, context.event.channel.asTextChannel(), context.event.author)
                    .flatMapLeft {
                        parsable.parseText(context, payload)
                    }
                    .mapLeft { command ->
                        buildAnnounceNode(context, command)
                    }
                    .mapLeft { command ->
                        buildUpdateProfileNode(context, command, context.event.author)
                    }
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext { parsed ->
            GuildManager.permissionGrantedRun(context.event.channel.asTextChannel(), Permission.MESSAGE_ADD_REACTION) {
                parsed.fold(
                    onLeft = { context.event.message.addReaction(EMOJI_CHECK).queue() },
                    onRight = { context.event.message.addReaction(EMOJI_CROSS).queue() },
                )
            }
        }
        .flatMap { parsed -> mono {
            parsed.fold(
                onLeft = { command ->
                    command.execute(
                        bot = context.bot,
                        config = context.config,
                        guild = context.guild,
                        user = context.user,
                        producer = DiscordMessageProducer,
                        messageRef = context.event.message.extractMessageRef(),
                        publishers = MonoPublisherSet(
                            publisher = { msg -> MessageActionAdaptor(context.event.message.reply(msg)) },
                        ),
                    )
                },
                onRight = { parseFailure ->
                    parseFailure.notice(
                        config = context.config,
                        producer = DiscordMessageProducer,
                        publisher = { msg -> MessageActionAdaptor(context.event.message.reply(msg)) },
                    )
                }
            ).fold(
                onSuccess = { (io, report) ->
                    export(context, io, null)
                    report
                },
                onFailure = { throwable ->
                    ErrorReport(throwable, context.guild)
                }
            ).apply {
                interactionSource = getEventAbbreviation(context.event::class)
                emittedTime = context.emittedTime
            }
        } }
}
