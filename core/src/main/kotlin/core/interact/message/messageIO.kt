package core.interact.message

import core.assets.MessageRef
import core.assets.VOID_MESSAGE_REF
import utils.structs.IO
import utils.structs.Option
import java.io.InputStream

typealias MessagePublisher<A, B> = (A) -> MessageIO<A, B>

typealias MessageEditPublisher<A, B> = (MessageRef) -> (A) -> MessageIO<A, B>

typealias ComponentPublisher<A, B> = (B) -> MessageIO<A, B>

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
    private val selfRef: MessageRef = VOID_MESSAGE_REF,
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

interface MessageIO<A, B> {

    fun addFile(file: InputStream, name: String): MessageIO<A, B>

    fun addButtons(buttons: B): MessageIO<A, B>

    fun launch(): IO<Unit>

    fun retrieve(): IO<Option<MessageAdaptor<A, B>>>

}

interface MessageAdaptor<A, B> {

    val messageRef: MessageRef

    val original: A

    val buttons: B

    fun updateButtons(buttons: B): MessageIO<A, B>

}

enum class ButtonFlag {
    FREE, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN
}

typealias FocusedFields = List<List<Pair<String, ButtonFlag>>>
