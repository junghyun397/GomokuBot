package core.interact.message

import core.assets.Message
import java.io.InputStream

typealias MessagePublisher<A, B> = (A) -> MessageAction<A, B>

typealias MessageModifier<A, B> = suspend () -> MessageAdaptor<A, B>

interface MessageAction<A, B> {

    fun addFile(file: InputStream, name: String): MessageAction<A, B>

    fun addButtons(buttons: B): MessageAction<A, B>

    fun launch()

    suspend fun retrieve(): MessageAdaptor<A, B>

}

abstract class MessageAdaptor<A, B> {

    abstract val message: Message

    abstract val original: A

    abstract val buttons: B

    abstract fun updateButtons(buttons: B): MessageAction<A, B>

}

enum class ButtonFlag {
    FREE, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN
}

typealias FocusedFields = List<List<Pair<String, ButtonFlag>>>
