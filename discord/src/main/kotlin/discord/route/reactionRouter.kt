package discord.route

import core.assets.Message
import core.assets.MessageId
import core.interact.reports.CommandReport
import core.session.SessionManager
import core.session.entities.NavigationKind
import dev.minn.jda.ktx.await
import discord.assets.extractId
import discord.assets.extractUser
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
import utils.structs.Option
import utils.structs.asOption

fun reactionRouter(context: InteractionContext<MessageReactionAddEvent>): Mono<Tuple2<InteractionContext<MessageReactionAddEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        SessionManager.getNavigateState(context.bot.sessionRepository, Message(
            id = MessageId(context.event.messageIdLong),
            guildId = context.guild.id,
            channelId = context.event.textChannel.extractId()
        )).asOption()
            .map { when(it.navigationKind) {
                NavigationKind.BOARD -> it to FocusCommandParser
                NavigationKind.ABOUT, NavigationKind.SETTINGS -> it to NavigateCommandParser
            } }
            .toMono()
    )
        .flatMap { Mono.zip(
            it.t1.toMono(),
            mono { it.t2.flatMap { parsable ->
                parsable.second.parseReaction(it.t1, parsable.first).flatMap { command ->
                    val message = it.t1.event.retrieveMessage().await()
                    if (message.author.idLong == it.t1.event.jda.selfUser.idLong)
                        Option(command to message)
                    else
                        Option.Empty
                }
            } }
        ) }
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .reaction.removeReaction(it.t1.event.user!!).queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), it.t2.getOrException().second.toMono(), mono { it.t2.getOrException().first
            .execute(
                bot = it.t1.bot,
                config = it.t1.config,
                user = it.t1.event.user!!.extractUser(),
                message = async { DiscordMessageAdaptor(it.t2.getOrException().second) },
                producer = DiscordMessageProducer,
                publisher = { msg -> MessageActionAdaptor(it.t1.event.channel.sendMessage(msg)) }
            )
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t3.map { combined ->
            consumeIO(it.t1, combined.first, it.t2)
            combined.second
        } }) }
