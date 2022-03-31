package core.interact.message

import core.assets.MessageId
import java.io.File

typealias MessagePublisher<A, B> = (A) -> MessageAction<B>

interface MessageAction<in T> {
    fun addFile(file: File): MessageAction<T>
    fun addButtons(buttons: T): MessageAction<T>
    fun launch()
    suspend fun retrieve(): MessageId
}

enum class SpotInfo {
        FREE, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN
}

typealias MiniBoardStatusMap = Array<Array<Pair<String, SpotInfo>>>
