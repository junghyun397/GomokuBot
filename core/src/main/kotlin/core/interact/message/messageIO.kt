package core.interact.message

import java.io.File

typealias MessagePublisher<A, B> = (A) -> MessageAction<B>

interface MessageAction<in T> {
    fun addFile(file: File): MessageAction<T>
    fun addButtons(buttons: T): MessageAction<T>
    fun send()
}
