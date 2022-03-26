@file:Suppress("unused")

package utils.monads

sealed class Maybe<out T> {

    val isDefined: Boolean get() = this is Just

    val isEmpty: Boolean get() = this is Nothing

    fun getOrNull(): T? = (this as Just).value

    inline fun <R> map(mapper: (T) -> R): Maybe<R> =
        when(this) {
            is Just -> Just(mapper(this.value))
            is Nothing -> this
        }

    inline fun <R> flatMap(mapper: (T) -> Maybe<R>): Maybe<R> =
        when(this) {
            is Just -> mapper(this.value)
            is Nothing -> this
        }

    inline fun <R> fold(onDefined: (T) -> R, onEmpty: () -> R): R =
        when(this) {
            is Just -> onDefined(this.value)
            is Nothing -> onEmpty()
        }

    data class Just<out T>(val value: T) : Maybe<T>()

    object Nothing : Maybe<kotlin.Nothing>()

}
