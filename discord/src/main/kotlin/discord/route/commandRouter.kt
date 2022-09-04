@file:Suppress("DuplicatedCode")

package discord.route

import core.assets.VOID_MESSAGE_REF
import core.database.repositories.AnnounceRepository
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.commands.AnnounceCommand
import core.interact.commands.Command
import core.interact.commands.ResponseFlag
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
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import utils.lang.component1
import utils.lang.component2
import utils.lang.component3
import utils.structs.*
import java.util.concurrent.TimeUnit

@JvmName("buildPermissionNodeSlash")
private fun buildPermissionNode(tuple: Tuple2<InteractionContext<SlashCommandInteractionEvent>, Option<ParsableCommand>>) =
    tuple.mapT2 { parsable ->
        buildPermissionNode(tuple.t1, tuple.t1.event.channel.asTextChannel(), tuple.t1.event.user, parsable.getOrException())
    }

@JvmName("buildPermissionNodeText")
private fun buildPermissionNode(tuple: Tuple3<InteractionContext<MessageReceivedEvent>, Option<ParsableCommand>, List<String>>) =
    tuple.mapT2 { parsable ->
        buildPermissionNode(tuple.t1, tuple.t1.event.channel.asTextChannel(), tuple.t1.event.author, parsable.getOrException())
    }

private fun buildPermissionNode(context: InteractionContext<*>, channel: TextChannel, jdaUser: User, parsableCommand: ParsableCommand) =
    GuildManager.permissionNotGrantedRun(channel, Permission.MESSAGE_SEND) {
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
    }.fold(
        onDefined = { Either.Right(it) },
        onEmpty = { Either.Left(parsableCommand) }
    )

private fun <T : Event> buildAnnounceNode(tuple: Tuple2<InteractionContext<T>, Either<Command, DiscordParseFailure>>) =
    when {
        tuple.t2.isLeft && (tuple.t1.user.announceId ?: -1) < (AnnounceRepository.getLatestAnnounceId(tuple.t1.bot.dbConnection) ?: -1) ->
            tuple.mapT2 { parsed -> parsed.mapLeft { AnnounceCommand(it) } }
        else -> tuple
    }

@JvmName("buildUpdateProfileNodeSlash")
private fun buildUpdateProfileNode(tuple: Tuple2<InteractionContext<SlashCommandInteractionEvent>, Either<Command, DiscordParseFailure>>) =
    buildUpdateProfileNode(tuple, tuple.t1.event.user)

@JvmName("buildUpdateProfileNodeText")
private fun buildUpdateProfileNode(tuple: Tuple2<InteractionContext<MessageReceivedEvent>, Either<Command, DiscordParseFailure>>) =
    buildUpdateProfileNode(tuple, tuple.t1.event.author)

private fun <T : Event> buildUpdateProfileNode(
    tuple: Tuple2<InteractionContext<T>, Either<Command, DiscordParseFailure>>,
    jdaUser: User,
): Mono<Tuple2<InteractionContext<T>, Either<Command, DiscordParseFailure>>> {
    val user = jdaUser.extractProfile(uid = tuple.t1.user.id, announceId = tuple.t1.user.announceId)
    val guild = tuple.t1.jdaGuild.extractProfile(uid = tuple.t1.guild.id)

    return when {
        user != tuple.t1.user -> mono {
            UserProfileRepository.upsertUser(tuple.t1.bot.dbConnection, user)
            Tuples.of(tuple.t1.copy(user = user), tuple.t2)
        }
        guild != tuple.t1.guild -> mono {
            GuildProfileRepository.upsertGuild(tuple.t1.bot.dbConnection, guild)
            Tuples.of(tuple.t1.copy(guild = guild), tuple.t2)
        }
        else -> tuple.toMono()
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
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.name,
            container = context.config.language.container
        ).toMono()
    )
        .filter { (_, maybeParsable) -> maybeParsable.isDefined }
        .map(::buildPermissionNode)
        .flatMap { (context, tuple) -> Mono.zip(context.toMono(), mono {
            tuple.flatMapLeft { parsable ->
                parsable.parseSlash(context)
            }
        }) }
        .doOnNext { (context, parsed) ->
            parsed.onLeft { if (it.responseFlag == ResponseFlag.DEFER) context.event.deferReply().queue() }
        }
        .flatMap(::buildUpdateProfileNode)
        .map(::buildAnnounceNode)
        .flatMap { (context, parsed) -> Mono.zip(context.toMono(), mono { parsed.fold(
            onLeft = { command ->
                command.execute(
                    bot = context.bot,
                    config = context.config,
                    guild = context.guild,
                    user = context.user,
                    producer = DiscordMessageProducer,
                    messageRef = VOID_MESSAGE_REF,
                    publishers = when (command.responseFlag) {
                        ResponseFlag.DEFER -> DiPublisherSet(
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
        ) }) }
        .flatMap { (context, result) ->
            mono {
                result.fold(
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
            }
        }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<InteractionReport> =
    run {
        val messageRaw = context.event.message.contentRaw

        when {
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
    }.let { payload ->
        Mono.zip(
            context.toMono(),
            matchCommand(
                command = payload.first(),
                container = context.config.language.container
            ).toMono(),
            payload.toMono()
        )
    }
        .filter { (_, maybeParsable, _) -> maybeParsable.isDefined }
        .map(::buildPermissionNode)
        .flatMap { (context, tuple, payload) -> Mono.zip(context.toMono(), mono {
            tuple.flatMapLeft { parsable ->
                parsable.parseText(context, payload)
            }
        }) }
        .doOnNext { (context, parsed) ->
            GuildManager.permissionGrantedRun(context.event.channel.asTextChannel(), Permission.MESSAGE_ADD_REACTION) {
                parsed.fold(
                    onLeft = { context.event.message.addReaction(EMOJI_CHECK).queue() },
                    onRight = { context.event.message.addReaction(EMOJI_CROSS).queue() },
                )
            }
        }
        .flatMap(::buildUpdateProfileNode)
        .map(::buildAnnounceNode)
        .flatMap { (context, parsed) -> Mono.zip(context.toMono(), mono { parsed.fold(
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
        ) }) }
        .flatMap { (context, result) ->
            mono {
                result.fold(
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
            }
        }
