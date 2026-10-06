package core.interact.message

import core.assets.MessageRef

typealias MessagePublisher = (AppMessage) -> MessageAction

typealias MessageEditPublisher = (MessageRef) -> MessagePublisher

interface PublisherSet {

    val plain: MessagePublisher

    val windowed: MessagePublisher

    val edit: MessageEditPublisher

}

data class AdaptivePublisherSet(
    override val plain: MessagePublisher,
    override val windowed: MessagePublisher,
    private val editSelf: MessagePublisher = { throw IllegalAccessError() },
    private val editGlobal: MessageEditPublisher = { throw IllegalAccessError() },
    private val selfRef: MessageRef? = null,
) : PublisherSet {

    override val edit: MessageEditPublisher get() = { ref ->
        when (ref) {
            this.selfRef -> this.editSelf
            else -> this.editGlobal(ref)
        }
    }

}

data class MonoPublisherSet(
    private val publisher: MessagePublisher,
    private val editGlobal: MessageEditPublisher
) : PublisherSet {

    override val plain: MessagePublisher = this.publisher

    override val windowed: MessagePublisher = this.publisher

    override val edit: MessageEditPublisher = this.editGlobal

}
