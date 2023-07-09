package core.interact.message

import core.assets.DUMMY_MESSAGE_REF
import core.assets.MessageRef

typealias MessagePublisher<A, B> = (A) -> MessageBuilder<A, B>

typealias MessageEditPublisher<A, B> = (MessageRef) -> (A) -> MessageBuilder<A, B>

typealias ComponentPublisher<A, B> = (B) -> MessageBuilder<A, B>

interface PublisherSet<A, B> {

    val plain: MessagePublisher<A, B>

    val windowed: MessagePublisher<A, B>

    val edit: MessageEditPublisher<A, B>

    val component: ComponentPublisher<A, B>

}

data class AdaptivePublisherSet<A, B>(
    override val plain: MessagePublisher<A, B>,
    override val windowed: MessagePublisher<A, B>,
    override val component: ComponentPublisher<A, B> = { throw IllegalAccessError() },
    private val editSelf: MessagePublisher<A, B> = { throw IllegalAccessError() },
    private val editGlobal: MessageEditPublisher<A, B> = { throw IllegalAccessError() },
    private val selfRef: MessageRef = DUMMY_MESSAGE_REF,
) : PublisherSet<A, B> {

    override val edit: MessageEditPublisher<A, B> get() = { ref ->
        when (ref) {
            this.selfRef -> this.editSelf
            else -> this.editGlobal(ref)
        }
    }

}

data class MonoPublisherSet<A, B>(
    private val publisher: MessagePublisher<A, B>,
    private val editGlobal: MessageEditPublisher<A, B>
) : PublisherSet<A, B> {

    override val plain: MessagePublisher<A, B> = this.publisher

    override val windowed: MessagePublisher<A, B> = this.publisher

    override val edit: MessageEditPublisher<A, B> = this.editGlobal

    override val component: ComponentPublisher<A, B> get() { throw IllegalAccessError() }

}
