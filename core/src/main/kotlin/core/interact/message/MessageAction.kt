package core.interact.message

import arrow.core.raise.Effect
import arrow.core.raise.effect

interface MessageAction {

    fun launch(): Effect<Nothing, Unit>

    fun retrieve(): Effect<Nothing, SentMessage?>

}

class DeferredMessageAction(private val build: () -> MessageAction) : MessageAction {

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@DeferredMessageAction.build().launch().bind()
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        this@DeferredMessageAction.build().retrieve().bind()
    }

}
