@file:Suppress("unused")

package utils.structs

sealed class Option<out T> {

    val isDefined: Boolean get() = this is Some

    val isEmpty: Boolean get() = this is Empty

    fun getOrNull(): T? = (this as Some).value

    fun getOrException(): T =
        when (this) {
            is Some -> this.value
            is Empty -> throw NullPointerException()
        }

    inline fun <R> map(mapper: (T) -> R): Option<R> =
        when (this) {
            is Some -> Some(mapper(this.value))
            is Empty -> this
        }

    inline fun <R> flatMap(mapper: (T) -> Option<R>): Option<R> =
        when (this) {
            is Some -> mapper(this.value)
            is Empty -> this
        }

    inline fun <R> fold(onDefined: (T) -> R, onEmpty: () -> R): R =
        when (this) {
            is Some -> onDefined(this.value)
            is Empty -> onEmpty()
        }

    data class Some<out T>(val value: T) : Option<T>()

    object Empty : Option<Nothing>()

}

inline fun <T> Option<T>.foldLeft(onEmpty: () -> T): T =
    when(this) {
        is Option.Some -> this.value
        is Option.Empty -> onEmpty()
    }

inline fun <T> Option<T>.orElse(produce: () -> Option<T>): Option<T> =
    when (this) {
        is Option.Some -> this
        is Option.Empty -> produce()
    }

fun <T> T?.asOption(): Option<T> = this?.let { Option.Some(this) } ?: Option.Empty
