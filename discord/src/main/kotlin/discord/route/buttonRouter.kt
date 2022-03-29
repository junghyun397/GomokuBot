package discord.route

import core.interact.reports.CommandReport
import discord.assets.extractId
import discord.interact.InteractionContext
import discord.interact.command.parsers.SetCommandParser
import discord.interact.message.DiscordMessageBinder
import discord.interact.message.WebHookRestActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import utils.monads.Option

private fun matchAction(prefix: String) =
    when(prefix) {
        "s" -> Option.Some(SetCommandParser)
        else -> Option.Empty
    }

fun buttonInteractionRouter(context: InteractionContext<ButtonInteractionEvent>): Mono<Tuple2<InteractionContext<ButtonInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchAction(
            prefix = context.event.id.split("-").first()
        ).toMono()
    )
        .map { Tuples.of(
            it.t1,
            it.t2.flatMap { parsable ->
                parsable.parse(it.t1.event)
            }
        ) }
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.getOrNull()!!
            .execute(
                context = it.t1.botContext,
                config = it.t1.guildConfig,
                userId = it.t1.event.user.extractId(),
                binder = DiscordMessageBinder
            ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
        }) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.map { combined ->
            combined.first.run()
            combined.second
        } }) }
