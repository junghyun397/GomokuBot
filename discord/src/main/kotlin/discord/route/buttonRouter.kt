package discord.route

import core.interact.reports.CommandReport
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.WebHookRestActionAdaptor
import discord.interact.parse.parsers.AcceptCommandParser
import discord.interact.parse.parsers.SetCommandParser
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.structs.Option

private fun matchAction(prefix: String) =
    when(prefix) {
        "s" -> Option.Some(SetCommandParser)
        "a" -> Option.Some(AcceptCommandParser)
        else -> Option.Empty
    }

fun buttonInteractionRouter(context: InteractionContext<ButtonInteractionEvent>): Mono<Tuple2<InteractionContext<ButtonInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchAction(
            prefix = context.event.id.split("-").first()
        ).toMono()
    )
        .flatMap { Mono.zip(
            it.t1.toMono(),
            mono { it.t2.flatMap { parsable ->
                parsable.parseButton(it.t1)
            } }
        ) }
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.getOrNull()!!
            .execute(
                context = it.t1.botContext,
                config = it.t1.config,
                user = it.t1.event.user.extractUser(),
                producer = DiscordMessageProducer
            ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            processIO(it.t1, combined.first, it.t1.event.message)
            combined.second
        } }) }
