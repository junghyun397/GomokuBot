@file:Suppress("DuplicatedCode")

package discord.route

import core.assets.DUMMY_MESSAGE_REF
import core.database.repositories.AnnounceRepository
import core.interact.commands.*
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.AdaptivePublisherSet
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import discord.assets.*
import discord.interact.GuildManager
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.TransMessagePublisher
import discord.interact.message.WebHookMessageCreateAdaptor
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import utils.assets.LinuxTime
import utils.lang.replaceIf
import utils.structs.*
import java.util.concurrent.TimeUnit

private fun buildPermissionNode(context: UserInteractionContext<*>, parsableCommand: ParsableCommand, channel: GuildMessageChannel, jdaUser: User): Either<ParsableCommand, DiscordParseFailure> =
    GuildManager.permissionDependedRun(
        channel, Permission.MESSAGE_SEND,
        onMissed = { Either.Right(
            DiscordParseFailure(parsableCommand.name, "message permission not granted in $channel", context.guild, context.user) { _, _, container ->
                IO {
                    jdaUser.openPrivateChannel()
                        .flatMap { privateChannel ->
                            DiscordMessagingService.sendPermissionNotGrantedEmbed(
                                publisher = { msg -> privateChannel.sendMessage(msg.buildCreate()) },
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

private fun <T : Event> buildAnnounceNode(context: UserInteractionContext<T>, command: Command): Command =
    command.replaceIf((context.user.announceId ?: -1) < (AnnounceRepository.getLatestAnnounceId(context.bot.dbConnection) ?: -1)) {
        AnnounceCommand(command)
    }

private fun <T : Event> buildUpdateProfileNode(context: UserInteractionContext<T>, jdaUser: User, command: Command): Command {
    val user = jdaUser.extractProfile(uid = context.user.id, announceId = context.user.announceId)
    val guild = context.jdaGuild.extractProfile(uid = context.guild.id)

    val maybeThenUser = Option.cond(user != context.user) { user }
    val maybeThenGuild = Option.cond(guild != context.guild) { guild }

    return command.replaceIf(maybeThenUser.isDefined || maybeThenGuild.isDefined) {
        UpdateProfileCommand(command, maybeThenUser, maybeThenGuild)
    }
}

private suspend fun <T : Event> buildUpdateCommandsNode(context: UserInteractionContext<T>, command: Command): Command {
    if (context.guild.id in GuildManager.updateCommandBypassGuilds)
        return command

    GuildManager.updateCommandBypassGuilds += context.guild.id
    
    val (deprecates, adds) = GuildManager.buildCommandUpdates(context.jdaGuild, context.config.language.container)
    
    if (deprecates.isEmpty() && adds.isEmpty())
        return command

    return UpdateCommandsCommand(command, deprecates.map { it.name }, adds.map { it.getLocalizedName(Language.ENG.container) })
}

private fun matchCommand(command: String, container: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        "help" -> Option.Some(HelpCommandParser)
        container.helpCommand() -> Option.Some(HelpCommandParser)
        container.settingsCommand() -> Option.Some(SettingsCommandParser)
        container.startCommand() -> Option.Some(StartCommandParser)
        "s" -> Option.Some(SetCommandParser)
        container.resignCommand() -> Option.Some(ResignCommandParser)
        container.languageCommand() -> Option.Some(LangCommandParser)
        container.styleCommand() -> Option.Some(StyleCommandParser)
        container.rankCommand() -> Option.Some(RankCommandParser)
        container.ratingCommand() -> Option.Some(RatingCommandParser)
        container.replayCommand() -> Option.Some(ReplayListCommandParser)
        "debug" -> Option.Some(DebugCommandParser)
        else -> Option.Empty
    }

fun slashCommandRouter(context: UserInteractionContext<SlashCommandInteractionEvent>): Mono<Report> =
    mono {
        matchCommand(context.event.name, context.config.language.container)
            .map { parsable ->
                buildPermissionNode(context, parsable, context.event.channel.asGuildMessageChannel(), context.event.user)
                    .flatMapLeft {
                        parsable.parseSlash(context)
                    }
                    .mapLeft { command ->
                        buildAnnounceNode(context,
                            buildUpdateProfileNode(context, context.event.user,
                                buildUpdateCommandsNode(context, command)
                            )
                        )
                    }
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext { parsed ->
            parsed.onLeft { command ->
                val responseFlag = command.responseFlag

                if (responseFlag is ResponseFlag.Defer)
                    context.event.deferReply().setEphemeral(responseFlag.windowed).queue()
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
                        service = DiscordMessagingService,
                        messageRef = DUMMY_MESSAGE_REF,
                        publishers = when (command.responseFlag) {
                            is ResponseFlag.Defer -> AdaptivePublisherSet(
                                plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                                windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                                editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } },
                            )
                            else -> AdaptivePublisherSet(
                                plain = TransMessagePublisher(
                                    head = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate())) },
                                    tail = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) }
                                ),
                                windowed = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                                editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } }
                            )
                        }
                    )
                },
                onRight = { parseFailure ->
                    parseFailure.notice(
                        config = context.config,
                        service = DiscordMessagingService,
                        publisher = TransMessagePublisher(
                            head = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                            tail = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) }
                        )
                    )
                }
            ).fold(
                onSuccess = { (io, report) ->
                    executeIO(context.discordConfig, io, context.jdaGuild)
                    report
                },
                onFailure = { throwable ->
                    ErrorReport(throwable, context.guild)
                }
            ).apply {
                interactionSource = context.source
                emittedTime = context.emittedTime
                apiTime = LinuxTime.now()
            }
        } }

fun textCommandRouter(context: UserInteractionContext<MessageReceivedEvent>): Mono<Report> {
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
                buildPermissionNode(context, parsable, context.event.channel.asGuildMessageChannel(), context.event.author)
                    .flatMapLeft {
                        parsable.parseText(context, payload)
                    }
                    .mapLeft { command ->
                        buildUpdateCommandsNode(context,
                            buildUpdateProfileNode(context, context.event.author,
                                buildAnnounceNode(context, command)
                            )
                        )
                    }
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext { parsed ->
            GuildManager.permissionGrantedRun(context.event.channel.asGuildMessageChannel(), Permission.MESSAGE_ADD_REACTION) {
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
                        service = DiscordMessagingService,
                        messageRef = context.event.message.extractMessageRef(),
                        publishers = MonoPublisherSet(
                            publisher = { msg -> MessageCreateAdaptor(context.event.message.reply(msg.buildCreate())) },
                            editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } },
                        ),
                    )
                },
                onRight = { parseFailure ->
                    parseFailure.notice(
                        config = context.config,
                        service = DiscordMessagingService,
                        publisher = { msg -> MessageCreateAdaptor(context.event.message.reply(msg.buildCreate())) },
                    )
                }
            ).fold(
                onSuccess = { (io, report) ->
                    executeIO(context.discordConfig, io, context.jdaGuild)
                    report
                },
                onFailure = { throwable ->
                    ErrorReport(throwable, context.guild)
                }
            ).apply {
                interactionSource = context.source
                emittedTime = context.emittedTime
                apiTime = LinuxTime.now()
            }
        } }
}
