@file:Suppress("DuplicatedCode")

package discord.route

import core.assets.UNICODE_CHECK
import core.assets.UNICODE_CROSS
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import dev.minn.jda.ktx.coroutines.await
import discord.assets.COMMAND_PREFIX
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.*
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.lang.component1
import utils.lang.component2
import utils.lang.component3
import utils.structs.*
import java.util.concurrent.TimeUnit

private fun buildPermissionNode(context: InteractionContext<*>, channel: TextChannel, jdaUser: User, parsableCommand: ParsableCommand) =
    GuildManager.permissionNotGrantedRun(channel, Permission.MESSAGE_SEND) {
        DiscordParseFailure(parsableCommand.name, "message permission not granted in $channel", context.user) { _, _, container ->
            IO {
                jdaUser.openPrivateChannel()
                    .flatMap { privateChannel -> DiscordMessageProducer.sendPermissionNotGrantedEmbed(
                        publisher = { msg -> privateChannel.sendMessage(msg) },
                        container = container,
                        channelName = channel.name
                    ) }
                    .delay(1, TimeUnit.MINUTES)
                    .flatMap(Message::delete)
                    .queue()

                Order.Unit
            }
        }
    }

private fun matchCommand(command: String, container: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        "help" -> Option(HelpCommandParser)
        container.helpCommand() -> Option(HelpCommandParser)
        container.configCommand() -> Option(ConfigCommandParser)
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

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.name,
            container = context.config.language.container
        ).toMono()
    )
        .filter { (_, parsable) -> parsable.isDefined }
        .flatMap { (context, parsable) -> Mono.zip(context.toMono(), mono {
            buildPermissionNode(context, context.event.textChannel, context.event.user, parsable.getOrException()).fold(
                onDefined = { failure -> Either.Right(failure) },
                onEmpty = { parsable.getOrException()
                    .parseSlash(context)
                }
            )
        }) }
        .doOnNext { (context, parsed) ->
            if (parsed.isLeft) context.event.deferReply().queue()
        }
        .flatMap { (context, parsed) -> Mono.zip(context.toMono(), mono { parsed.fold(
            onLeft = { command ->
                command.execute(
                    bot = context.bot,
                    config = context.config,
                    guild = context.guild,
                    user = context.user,
                    producer = DiscordMessageProducer,
                    message = async { DiscordMessageAdaptor(context.event.hook.retrieveOriginal().await()) },
                    publisher = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) },
                    editPublisher = { msg -> WebHookUpdateActionAdaptor(context.event.hook.editOriginal(msg)) },
                )
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = context.config,
                    producer = DiscordMessageProducer,
                    publisher = { msg -> ReplyActionAdaptor(context.event.reply(msg)) }
                )
            }
        ) }) }
        .flatMap { (context, result) ->
            Mono.zip(context.toMono(), mono { result.map { (io, report) ->
                export(context, io, null)
                report
            } })
        }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
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
        .filter { (_, parsable, _) -> parsable.isDefined }
        .flatMap { (context, parsable, payload) -> Mono.zip(context.toMono(), mono {
            buildPermissionNode(context, context.event.textChannel, context.event.author, parsable.getOrException()).fold(
                onDefined = { failure -> Either.Right(failure) },
                onEmpty = { parsable.getOrException()
                    .parseText(context, payload)
                }
            )
        }) }
        .doOnNext { (context, parsed) ->
            GuildManager.permissionGrantedRun(context.event.textChannel, Permission.MESSAGE_ADD_REACTION) {
                parsed.fold(
                    onLeft = { context.event.message.addReaction(UNICODE_CHECK).queue() },
                    onRight = { context.event.message.addReaction(UNICODE_CROSS).queue() }
                )
            }
        }
        .flatMap { (context, parsed) -> Mono.zip(context.toMono(), mono { parsed.fold(
            onLeft = { command ->
                command.execute(
                    bot = context.bot,
                    config = context.config,
                    guild = context.guild,
                    user = context.user,
                    message = async { DiscordMessageAdaptor(context.event.message) },
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(context.event.message.reply(msg)) },
                    editPublisher = { msg -> MessageActionAdaptor(context.event.message.editMessage(msg)) }
                )
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = context.config,
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(context.event.message.reply(msg)) }
                )
            }
        ) }) }
        .flatMap { (context, result) ->
            Mono.zip(context.toMono(), mono { result.map { (io, report) ->
                export(context, io, null)
                report
            } })
        }
