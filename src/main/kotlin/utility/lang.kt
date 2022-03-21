package utility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class Either <out L, out R> {

    val isLeft: Boolean get() = this is Left

    val isRight: Boolean get() = this is Right

    inline fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(this.value)
        is Right -> onRight(this.value)
    }

    data class Left<out L>(val value: L) : Either<L, Nothing>()

    data class Right<out R>(val value: R) : Either<Nothing, R>()

}

@JvmInline
value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {
    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()
}

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java.simpleName)
