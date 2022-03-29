@file:Suppress("unused")

package utils.monads

sealed class Option<out T> {

    val isDefined: Boolean get() = this is Some

    val isEmpty: Boolean get() = this is Empty

    fun getOrNull(): T? = (this as Some).value

    inline fun <R> map(mapper: (T) -> R): Option<R> =
        when(this) {
            is Some -> Some(mapper(this.value))
            is Empty -> this
        }

    inline fun <R> flatMap(mapper: (T) -> Option<R>): Option<R> =
        when(this) {
            is Some -> mapper(this.value)
            is Empty -> this
        }

    inline fun <R> fold(onDefined: (T) -> R, onEmpty: () -> R): R =
        when(this) {
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
