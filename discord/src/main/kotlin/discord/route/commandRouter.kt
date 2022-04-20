package discord.route

import core.assets.UNICODE_CHECK
import core.assets.UNICODE_CROSS
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import discord.assets.extractUser
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.message.WebHookActionAdaptor
import discord.interact.message.WebHookMessageUpdateActionAdaptor
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
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

fun buildPermissionNode(channel: TextChannel, user: User, parsableCommand: ParsableCommand): Option<DiscordParseFailure> =
    GuildManager.permissionNotGrantedRun(channel, Permission.MESSAGE_SEND) {
        DiscordParseFailure(parsableCommand.name, "message permission not granted", user.extractUser()) { _, _, container ->
            IO { user.openPrivateChannel()
                .flatMap { privateChannel -> DiscordMessageProducer.sendPermissionNotGrantedEmbed(
                    container = container,
                    channelName = channel.name
                ) { msg -> privateChannel.sendMessage(msg) } }
                .delay(1, TimeUnit.MINUTES)
                .flatMap(Message::delete)
                .queue()
                Order.Unit
            }
        }
    }

private fun matchCommand(command: String, languageContainer: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        "help" -> Option.Some(HelpCommandParser)
        languageContainer.helpCommand() -> Option.Some(HelpCommandParser)
        languageContainer.startCommand() -> Option.Some(StartCommandParser)
        "s" -> Option.Some(SetCommandParser)
        languageContainer.resignCommand() -> Option.Some(ResignCommandParser)
        languageContainer.languageCommand() -> Option.Some(LangCommandParser)
        languageContainer.styleCommand() -> Option.Some(StyleCommandParser)
        languageContainer.rankCommand() -> Option.Some(RankCommandParser)
        languageContainer.ratingCommand() -> Option.Some(RatingCommandParser)
        "debug" -> Option.Some(DebugCommandParser)
        else -> Option.Empty
    }

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.name,
            languageContainer = context.config.language.container
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
                    context = it.t1.botContext,
                    config = it.t1.config,
                    user = it.t1.event.user.extractUser(),
                    producer = DiscordMessageProducer,
                    publisher = { msg -> WebHookActionAdaptor(it.t1.event.hook.sendMessage(msg)) },
                    modifier = { msg -> WebHookMessageUpdateActionAdaptor(it.t1.event.hook.editOriginal(msg)) },
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
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.message.contentRaw.split(" ")[0].substring(1),
            languageContainer = context.config.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .flatMap { Mono.zip(it.t1.toMono(), mono {
            buildPermissionNode(it.t1.event.textChannel, it.t1.event.author, it.t2.getOrException()).fold(
                onDefined = { failure -> Either.Right(failure) },
                onEmpty = { it.t2.getOrException()
                    .parseText(it.t1)
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
                    context = it.t1.botContext,
                    config = it.t1.config,
                    user = it.t1.event.author.extractUser(),
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(it.t1.event.message.reply(msg)) },
                    modifier = { msg -> MessageActionAdaptor(it.t1.event.message.editMessage(msg)) }
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
