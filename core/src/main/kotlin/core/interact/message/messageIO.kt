package core.interact.message

import core.assets.Message
import java.io.InputStream

typealias MessagePublisher<A, B> = (A) -> MessageAction<B>

typealias MessageModifier<A, B> = MessagePublisher<A, B>

interface MessageAction<in T> {
    fun addFile(file: InputStream, name: String): MessageAction<T>
    fun addButtons(buttons: T): MessageAction<T>
    fun launch()
    suspend fun retrieve(): Message
}

enum class ButtonFlag {
    FREE, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN
}

typealias FocusedFields = List<List<Pair<String, ButtonFlag>>>
