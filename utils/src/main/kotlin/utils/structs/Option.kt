@file:Suppress("unused")

package utils.structs

sealed class Option<out T> {

    val isDefined: Boolean get() = this is Some

    val isEmpty: Boolean get() = this is Empty

    fun getOrNull(): T? =
        when (this) {
            is Some -> this.value
            is Empty -> null
        }

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

    inline fun forEach(block: (T) -> Unit) {
        if (this.isDefined)
            block((this as Some).value)
    }

    inline fun filter(predicate: (T) -> Boolean) =
        if (this is Some && predicate(this.value)) this
        else Empty

    inline fun <T> Option<T>.getOrElse(onEmpty: () -> T): T =
        when (this) {
            is Some -> this.value
            is Empty -> onEmpty()
        }

    inline fun <T> Option<T>.orElse(produce: () -> Option<T>): Option<T> =
        when (this) {
            is Some -> this
            is Empty -> produce()
        }

    data class Some<out T>(val value: T) : Option<T>()

    object Empty : Option<Nothing>()

    companion object {

        fun <T> unit(value: T) =
            Some(value)

        operator fun <T> invoke(value: T) =
            unit(value)

    }

}

fun <T> T?.asOption(): Option<T> =
    if (this == null) Option.Empty
    else Option(this)

fun <T> Result<T>.toOption(): Option<T> =
    this.fold(
        onSuccess = { Option(it) },
        onFailure = { Option.Empty }
    )
