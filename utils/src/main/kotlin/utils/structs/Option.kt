@file:Suppress("unused")

package utils.structs

import utils.lang.and

sealed class Option<out T> {

    val isDefined: Boolean get() = this is Some

    val isEmpty: Boolean get() = this is Empty

    data class Some<out T>(val value: T) : Option<T>()

    object Empty : Option<Nothing>()

    companion object {

        fun <T> unit(value: T) =
            Some(value)

        operator fun <T> invoke(value: T) =
            unit(value)

        fun <A, B> zip(a: Option<A>, b: Option<B>) =
            when {
                a is Some && b is Some -> Some(a.value and b.value)
                else -> Empty
            }

    }

}

fun <T> Option<T>.getOrNull(): T? =
    when (this) {
        is Option.Some -> this.value
        is Option.Empty -> null
    }

fun <T> Option<T>.getOrException(): T =
    when (this) {
        is Option.Some -> this.value
        is Option.Empty -> throw NullPointerException()
    }

inline fun <T, R> Option<T>.map(mapper: (T) -> R): Option<R> =
    when (this) {
        is Option.Some -> Option.Some(mapper(this.value))
        is Option.Empty -> this
    }

inline fun <T, R> Option<T>.flatMap(mapper: (T) -> Option<R>): Option<R> =
    when (this) {
        is Option.Some -> mapper(this.value)
        is Option.Empty -> this
    }

inline fun <T, R> Option<T>.fold(onDefined: (T) -> R, onEmpty: () -> R): R =
    when (this) {
        is Option.Some -> onDefined(this.value)
        is Option.Empty -> onEmpty()
    }

inline fun <T> Option<T>.forEach(block: (T) -> Unit) {
    if (this.isDefined)
        block((this as Option.Some).value)
}

inline fun <T> Option<T>.filter(predicate: (T) -> Boolean) =
    when {
        this is Option.Some && predicate(this.value) -> this
        else -> Option.Empty
    }

inline fun <T> Option<T>.orElseGet(onEmpty: () -> T): T =
    when (this) {
        is Option.Some -> this.value
        is Option.Empty -> onEmpty()
    }

inline fun <T> Option<T>.orElse(produce: () -> Option<T>): Option<T> =
    when (this) {
        is Option.Some -> this
        is Option.Empty -> produce()
    }

@JvmName("nullableAsOption")
fun <T> T?.asOption(): Option<T> =
    if (this == null) Option.Empty
    else Option(this)

@JvmName("resultAsOption")
fun <T> Result<T>.asOption(): Option<T> =
    this.fold(
        onSuccess = { Option(it) },
        onFailure = { Option.Empty }
    )
