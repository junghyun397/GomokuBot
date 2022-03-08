package route

import BotContext
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

fun buildButtonInteractionHandler(botContext: BotContext): (ButtonInteractionEvent) -> Mono<Result<ButtonInteractionEvent>> =
    { event ->
        Mono.zip(event.toMono(), botContext.toMono())
            .flatMap { TODO() }
    }
