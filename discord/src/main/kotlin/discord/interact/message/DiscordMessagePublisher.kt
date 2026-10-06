package discord.interact.message

import core.assets.MessageRef
import core.interact.message.DeferredMessageAction
import core.interact.message.MessageAction
import core.interact.message.MessagePublisher
import core.interact.message.PublisherSet

typealias DiscordMessagePublisher = MessagePublisher

fun discordPublisher(publish: (DiscordMessageData) -> MessageAction): MessagePublisher = { message ->
    DeferredMessageAction { publish(DiscordMessageRenderer.render(message)) }
}

fun TransMessagePublisher(head: MessagePublisher, tail: MessagePublisher): MessagePublisher {
    var consumeTail = false
    return { message ->
        DeferredMessageAction {
            val publisher = if (consumeTail) tail else head
            consumeTail = true
            publisher(message)
        }
    }
}

class TransMessagePublisherSet(
    private val head: PublisherSet,
    private val tail: PublisherSet,
    private val selfRef: MessageRef? = null,
) : PublisherSet {

    private var consumeTail = false

    private fun selectSet(): PublisherSet {
        val publishers = if (this.consumeTail) this.tail else this.head
        this.consumeTail = true
        return publishers
    }

    override val plain: MessagePublisher = { message ->
        DeferredMessageAction { this.selectSet().plain(message) }
    }

    override val windowed: MessagePublisher = { message ->
        DeferredMessageAction { this.selectSet().windowed(message) }
    }

    override val edit: (MessageRef) -> MessagePublisher = { ref -> { message ->
        DeferredMessageAction {
            val publishers = if (ref == this.selfRef) this.selectSet() else this.tail
            publishers.edit(ref)(message)
        }
    } }

}
