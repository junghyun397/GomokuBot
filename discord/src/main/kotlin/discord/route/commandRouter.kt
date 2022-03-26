@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.command.ParsableCommand
import discord.interact.command.ParseFailure
import discord.interact.command.parsers.*
import discord.interact.message.DiscordMessageBinder
import discord.interact.message.MessageActionRestActionAdaptor
import discord.interact.message.WebHookRestActionAdaptor
import discord.utils.extractId
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import utils.assets.UNICODE_CHECK
import utils.assets.UNICODE_CROSS
import utils.monads.Either
import utils.monads.IO
import utils.monads.Maybe
import utils.monads.flatMapLeft
import java.util.concurrent.TimeUnit

private fun matchCommand(command: String, languageContainer: LanguageContainer): Maybe<ParsableCommand> =
    when (command.lowercase()) {
        languageContainer.helpCommand() -> Maybe.Just(HelpCommandParser)
        languageContainer.startCommand() -> Maybe.Just(StartCommandParser)
        "s" -> Maybe.Just(SetCommandParser)
        languageContainer.resignCommand() -> Maybe.Just(ResignCommandParser)
        languageContainer.langCommand() -> Maybe.Just(LangCommandParser)
        languageContainer.styleCommand() -> Maybe.Just(StyleCommandParser)
        languageContainer.rankCommand() -> Maybe.Just(RankCommandParser)
        languageContainer.ratingCommand() -> Maybe.Just(RatingCommandParser)
        else -> Maybe.Nothing
    }

private fun generatePermissionNode(user: User, guildChannel: GuildChannel, parsableCommand: ParsableCommand) =
    if (GuildManager.lookupPermission(guildChannel, Permission.MESSAGE_SEND)) Either.Left(parsableCommand)
    else Either.Right(ParseFailure(parsableCommand.name, "message permission not granted") { container, _ ->
        IO.unit {
            user.openPrivateChannel()
                .flatMap { channel -> DiscordMessageBinder.sendPermissionNotGrantedEmbed(
                    publisher = { msg -> channel.sendMessage(msg) },
                    container = container,
                    channelName = guildChannel.name
                ) }
                .delay(1, TimeUnit.MINUTES)
                .flatMap(Message::delete)
                .queue()
        }
    } )

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.name,
            languageContainer = context.guildConfig.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono {
            generatePermissionNode(it.t1.event.user, it.t1.event.guildChannel, it.t2.getOrNull()!!).flatMapLeft { parsable ->
                parsable.parse(it.t1.event, it.t1.guildConfig.language.container)
            }
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    botContext = it.t1.botContext,
                    guildConfig = it.t1.guildConfig,
                    userId = it.t1.event.user.extractId(),
                    messageBinder = DiscordMessageBinder
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            } ,
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig,
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            }
        ) }) }
        .map { Tuples.of(it.t1, it.t2.map { combined ->
            combined.first.run()
            combined.second
        }) }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.message.contentRaw.split(" ")[0].substring(1),
            languageContainer = context.guildConfig.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .flatMap { Mono.zip(it.t1.toMono(), mono {
            generatePermissionNode(it.t1.event.author, it.t1.event.guildChannel, it.t2.getOrNull()!!).flatMapLeft { parsable ->
                parsable.parse(event = it.t1.event, languageContainer = it.t1.guildConfig.language.container)
            }
        }) }
        .doOnNext {
            GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_ADD_REACTION) { _ ->
                if (it.t2.isLeft) it.t1.event.message.addReaction(UNICODE_CHECK).queue()
                else it.t1.event.message.addReaction(UNICODE_CROSS).queue()
            }
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    botContext = it.t1.botContext,
                    guildConfig = it.t1.guildConfig,
                    userId = it.t1.event.author.extractId(),
                    messageBinder = DiscordMessageBinder,
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig,
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            }
        ) }) }
        .map { Tuples.of(it.t1, it.t2.map { combined ->
            combined.first.run()
            combined.second
        }) }
