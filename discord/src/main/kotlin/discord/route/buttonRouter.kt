package discord.route

import core.interact.reports.ButtonInteractionReport
import discord.interact.InteractionContext
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

fun buttonInteractionRouter(context: InteractionContext<ButtonInteractionEvent>): Mono<Tuple2<InteractionContext<ButtonInteractionEvent>, Result<ButtonInteractionReport>>> =
    TODO()