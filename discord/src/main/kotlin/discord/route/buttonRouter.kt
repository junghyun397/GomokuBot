@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.reports.CommandReport
import dev.minn.jda.ktx.await
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageAdaptor
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.WebHookActionAdaptor
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.AcceptCommandParser
import discord.interact.parse.parsers.ConfigCommandParser
import discord.interact.parse.parsers.RejectCommandParser
import discord.interact.parse.parsers.SetCommandParser
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.structs.Option
import utils.structs.asOption

private fun matchAction(prefix: String): Option<EmbeddableCommand> =
    when (prefix) {
        "s" -> Option(SetCommandParser)
        "a" -> Option(AcceptCommandParser)
        "r" -> Option(RejectCommandParser)
        "p" -> Option(ConfigCommandParser)
        else -> Option.Empty
    }

fun buttonInteractionRouter(context: InteractionContext<GenericComponentInteractionCreateEvent>): Mono<Tuple2<InteractionContext<GenericComponentInteractionCreateEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        context.event.component.id.asOption().flatMap {
            matchAction(
                prefix = it.split("-").first()
            )
        }.toMono()
    )
        .flatMap { Mono.zip(
            it.t1.toMono(),
            mono { it.t2.flatMap { parsable ->
                parsable.parseButton(it.t1)
            } }
        ) }
        .filter { it.t2.isDefined }
        .doOnNext {
            it.t1.event
                .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.getOrException()
            .execute(
                bot = it.t1.bot,
                config = it.t1.config,
                user = it.t1.event.user.extractUser(),
                message = async { DiscordMessageAdaptor(it.t1.event.hook.retrieveOriginal().await()) },
                producer = DiscordMessageProducer
            ) { msg -> WebHookActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            consumeIO(it.t1, combined.first, it.t1.event.message)
            combined.second
        } }) }
