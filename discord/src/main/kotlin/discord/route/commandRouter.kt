package discord.route

import core.assets.UNICODE_CHECK
import core.assets.UNICODE_CROSS
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import discord.assets.extractId
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.command.ParsableCommand
import discord.interact.command.ParseFailure
import discord.interact.command.parsers.*
import discord.interact.message.DiscordMessageBinder
import discord.interact.message.MessageActionRestActionAdaptor
import discord.interact.message.WebHookRestActionAdaptor
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
import utils.monads.Either
import utils.monads.IO
import utils.monads.Option
import java.util.concurrent.TimeUnit

private inline fun withMessagePermissionNode(user: User, guildChannel: GuildChannel, parsableCommand: ParsableCommand, block: (ParsableCommand) -> Either<Command, ParseFailure>) =
    if (GuildManager.lookupPermission(guildChannel, Permission.MESSAGE_SEND)) block(parsableCommand)
    else Either.Right(ParseFailure(parsableCommand.name, "message permission not granted") { container, _ ->
        IO { user.openPrivateChannel()
            .flatMap { channel -> DiscordMessageBinder.sendPermissionNotGrantedEmbed(
                publisher = { msg -> channel.sendMessage(msg) },
                container = container,
                channelName = guildChannel.name
            ) }
            .delay(1, TimeUnit.MINUTES)
            .flatMap(Message::delete)
            .queue()
        }
    })

private fun matchCommand(command: String, languageContainer: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        languageContainer.helpCommand() -> Option.Some(HelpCommandParser)
        languageContainer.startCommand() -> Option.Some(StartCommandParser)
        "s" -> Option.Some(SetCommandParser)
        languageContainer.resignCommand() -> Option.Some(ResignCommandParser)
        languageContainer.langCommand() -> Option.Some(LangCommandParser)
        languageContainer.styleCommand() -> Option.Some(StyleCommandParser)
        languageContainer.rankCommand() -> Option.Some(RankCommandParser)
        languageContainer.ratingCommand() -> Option.Some(RatingCommandParser)
        else -> Option.Empty
    }

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
        .map { Tuples.of(
            it.t1,
            withMessagePermissionNode(it.t1.event.user, it.t1.event.guildChannel, it.t2.getOrNull()!!) { parsable ->
                parsable.parse(it.t1.event, it.t1.guildConfig.language.container)
            }
        ) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    context = it.t1.botContext,
                    config = it.t1.guildConfig,
                    userId = it.t1.event.user.extractId(),
                    binder = DiscordMessageBinder
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            } ,
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig,
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            combined.first.run()
            combined.second
        } }) }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.message.contentRaw.split(" ")[0].substring(1),
            languageContainer = context.guildConfig.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .map { Tuples.of(
            it.t1,
            withMessagePermissionNode(it.t1.event.author, it.t1.event.guildChannel, it.t2.getOrNull()!!) { parsable ->
                parsable.parse(event = it.t1.event, languageContainer = it.t1.guildConfig.language.container)
            }
        ) }
        .doOnNext {
            GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_ADD_REACTION) { _ ->
                if (it.t2.isLeft) it.t1.event.message.addReaction(UNICODE_CHECK).queue()
                else it.t1.event.message.addReaction(UNICODE_CROSS).queue()
            }
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    context = it.t1.botContext,
                    config = it.t1.guildConfig,
                    userId = it.t1.event.author.extractId(),
                    binder = DiscordMessageBinder,
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig,
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            }
        ) }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            combined.first.run()
            combined.second
        } }) }
