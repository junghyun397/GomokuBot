package core.interact.message

import core.assets.MessageRef
import utils.structs.IO
import utils.structs.Option
import java.io.InputStream

typealias MessagePublisher<A, B> = (A) -> MessageIO<A, B>

typealias ComponentPublisher<A, B> = (B) -> MessageIO<A, B>

interface PublisherSet<A, B> {

    val plain: MessagePublisher<A, B>

    val windowed: MessagePublisher<A, B>

    val edit: MessagePublisher<A, B>

    val component: ComponentPublisher<A, B>

}

data class PolyPublisherSet<A, B>(
    override val plain: MessagePublisher<A, B>,
    override val windowed: MessagePublisher<A, B>,
    override val edit: MessagePublisher<A, B>,
    override val component: ComponentPublisher<A, B>
) : PublisherSet<A, B>

data class DiPublisherSet<A, B>(
    override val plain: MessagePublisher<A, B>,
    override val windowed: MessagePublisher<A, B>,
) : PublisherSet<A, B> {

    override val component: ComponentPublisher<A, B> get() = throw IllegalAccessError()

    override val edit: MessagePublisher<A, B> get() = throw IllegalAccessError()
}

data class MonoPublisherSet<A, B>(
    private val publisher: MessagePublisher<A, B>
) : PublisherSet<A, B> {

    override val plain: MessagePublisher<A, B> get() = this.publisher

    override val windowed: MessagePublisher<A, B> get() = this.publisher

    override val edit: MessagePublisher<A, B> get() = this.publisher

    override val component: ComponentPublisher<A, B> get() { throw IllegalAccessError() }

}

interface MessageIO<A, B> {

    fun addFile(file: InputStream, name: String): MessageIO<A, B>

    fun addButtons(buttons: B): MessageIO<A, B>

    fun launch(): IO<Unit>

    fun retrieve(): IO<Option<MessageAdaptor<A, B>>>

}

abstract class MessageAdaptor<A, B> {

    abstract val messageRef: MessageRef

    abstract val original: A

    abstract val buttons: B

    abstract fun updateButtons(buttons: B): MessageIO<A, B>

}

enum class ButtonFlag {
    FREE, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN
}

typealias FocusedFields = List<List<Pair<String, ButtonFlag>>>
