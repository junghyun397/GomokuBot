package discord.route

import core.assets.UNICODE_CHECK
import core.assets.UNICODE_CROSS
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import discord.assets.extractUser
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionRestActionAdaptor
import discord.interact.message.WebHookRestActionAdaptor
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.structs.Either
import utils.structs.Option

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
            mergeMessagePermission(it.t1.event.textChannel, it.t1.event.user, it.t2.getOrException()).fold(
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
                    producer = DiscordMessageProducer
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            } ,
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.config,
                    producer = DiscordMessageProducer
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            processIO(it.t1, combined.first, null)
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
            mergeMessagePermission(it.t1.event.textChannel, it.t1.event.author, it.t2.getOrException()).fold(
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
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.config,
                    producer = DiscordMessageProducer,
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            processIO(it.t1, combined.first, it.t1.event.message)
            combined.second
        } }) }
