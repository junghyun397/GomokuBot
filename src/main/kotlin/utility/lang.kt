package utility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class Either <out L, out R> {

    abstract val isLeft: Boolean
    abstract val isRight: Boolean

    inline fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

    data class Left<out L>(val value: L) : Either<L, Nothing>() {
        override val isLeft = true
        override val isRight = false
    }

    data class Right<out R>(val value: R) : Either<Nothing, R>() {
        override val isLeft = false
        override val isRight = true
    }

}

@JvmInline
value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {
    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()
}

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java.simpleName)
