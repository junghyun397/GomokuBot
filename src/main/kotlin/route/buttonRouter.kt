package route

import interact.reports.ButtonInteractionReport
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

fun buildButtonInteractionHandler(): (InteractionContext<ButtonInteractionEvent>) -> Mono<Tuple2<InteractionContext<ButtonInteractionEvent>, Result<ButtonInteractionReport>>> =
    { context ->
        TODO()
    }
