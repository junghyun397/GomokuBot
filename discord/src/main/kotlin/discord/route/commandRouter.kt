@file:Suppress("DuplicatedCode")

package discord.route

import core.assets.UNICODE_CHECK
import core.assets.UNICODE_CROSS
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import dev.minn.jda.ktx.await
import discord.assets.COMMAND_PREFIX
import discord.assets.extractUser
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageAdaptor
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.message.WebHookActionAdaptor
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
import utils.structs.Either
import utils.structs.IO
import utils.structs.Option
import java.util.concurrent.TimeUnit

private fun buildPermissionNode(channel: TextChannel, user: User, parsableCommand: ParsableCommand): Option<DiscordParseFailure> =
    GuildManager.permissionNotGrantedRun(channel, Permission.MESSAGE_SEND) {
        DiscordParseFailure(parsableCommand.name, "message permission not granted in $channel", user.extractUser()) { _, _, container ->
            IO {
                user.openPrivateChannel()
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
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono {
            buildPermissionNode(it.t1.event.textChannel, it.t1.event.user, it.t2.getOrException()).fold(
                onDefined = { failure -> Either.Right(failure) },
                onEmpty = { it.t2.getOrException()
                    .parseSlash(it.t1)
                }
            )
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    bot = it.t1.bot,
                    config = it.t1.config,
                    user = it.t1.event.user.extractUser(),
                    message = async { DiscordMessageAdaptor(it.t1.event.hook.retrieveOriginal().await()) },
                    producer = DiscordMessageProducer,
                    publisher = { msg -> WebHookActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
                )
            } ,
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.config,
                    producer = DiscordMessageProducer,
                    publisher = { msg -> WebHookActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
                )
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            consumeIO(it.t1, combined.first, null)
            combined.second
        } }) }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
    run {
        val messageRaw = context.event.message.contentRaw

        if (messageRaw.startsWith(COMMAND_PREFIX))
            messageRaw.drop(1).split(" ")
        else {
            val payload = messageRaw.drop(21).trimStart().split(" ")

            if (payload.first().isEmpty())
                listOf("help")
            else
                payload
        }
    }.let {
        Mono.zip(
            context.toMono(),
            matchCommand(
                command = it.first(),
                container = context.config.language.container
            ).toMono(),
            it.toMono()
        )
    }
        .filter { it.t2.isDefined }
        .flatMap { Mono.zip(it.t1.toMono(), mono {
            buildPermissionNode(it.t1.event.textChannel, it.t1.event.author, it.t2.getOrException()).fold(
                onDefined = { failure -> Either.Right(failure) },
                onEmpty = { it.t2.getOrException()
                    .parseText(it.t1, it.t3)
                }
            )
        }) }
        .doOnNext {
            GuildManager.permissionSafeRun(it.t1.event.textChannel, Permission.MESSAGE_ADD_REACTION) { _ ->
                if (it.t2.isLeft) it.t1.event.message.addReaction(UNICODE_CHECK).queue()
                else it.t1.event.message.addReaction(UNICODE_CROSS).queue()
            }
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    bot = it.t1.bot,
                    config = it.t1.config,
                    user = it.t1.event.author.extractUser(),
                    message = async { DiscordMessageAdaptor(it.t1.event.message) },
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(it.t1.event.message.reply(msg)) }
                )
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.config,
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(it.t1.event.message.reply(msg)) }
                )
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            consumeIO(it.t1, combined.first, null)
            combined.second
        } }) }
