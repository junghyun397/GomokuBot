package discord.route

import core.assets.MessageId
import core.assets.MessageRef
import core.interact.reports.CommandReport
import core.session.SessionManager
import core.session.entities.NavigationKind
import dev.minn.jda.ktx.coroutines.await
import discord.assets.extractId
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageAdaptor
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.parse.parsers.FocusCommandParser
import discord.interact.parse.parsers.NavigateCommandParser
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.lang.component1
import utils.lang.component2
import utils.lang.component3
import utils.structs.*

fun reactionRouter(context: InteractionContext<MessageReactionAddEvent>): Mono<Tuple2<InteractionContext<MessageReactionAddEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        SessionManager.getNavigateState(
            context.bot.sessions,
            MessageRef(
                id = MessageId(context.event.messageIdLong),
                guildId = context.guild.givenId,
                channelId = context.event.textChannel.extractId()
            )
        )
            .asOption()
            .map { it to when (it.navigationKind) {
                NavigationKind.BOARD -> FocusCommandParser
                NavigationKind.ABOUT, NavigationKind.SETTINGS -> NavigateCommandParser
            } }
            .toMono()
    )
        .flatMap { (context, navigate) -> Mono.zip(
            context.toMono(),
            mono { navigate.flatMap { (state, parsable) ->
                parsable.parseReaction(context, state).flatMap { command ->
                    val message = context.event.retrieveMessage().await()
                    if (message.author.idLong == context.event.jda.selfUser.idLong)
                        Option(command to message)
                    else
                        Option.Empty
                }
            } }
        ) }
        .filter { (_, command) -> command.isDefined }
        .doOnNext {  (context, _) ->
            context.event.reaction.removeReaction(context.event.user!!).queue()
        }
        .flatMap { (context, command) ->
            Mono.zip(context.toMono(), command.getOrException().second.toMono(), mono { command.getOrException().first
                .execute(
                    bot = context.bot,
                    config = context.config,
                    user = context.user,
                    guild = context.guild,
                    message = async { DiscordMessageAdaptor(command.getOrException().second) },
                    producer = DiscordMessageProducer,
                    publisher = { msg -> MessageActionAdaptor(context.event.channel.sendMessage(msg)) },
                    editPublisher = { msg -> MessageActionAdaptor(command.getOrException().second.editMessage(msg)) }
                )
            })
        }
        .flatMap { (context, message, result) ->
            Mono.zip(context.toMono(), mono { result.map { (io, report) ->
                export(context, io, message)
                report
            } })
        }
