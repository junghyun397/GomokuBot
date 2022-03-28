@file:Suppress("unused")

package utils.monads

sealed class Either<out L, out R> {

    val isLeft: Boolean get() = this is Left

    val isRight: Boolean get() = this is Right

    inline fun <A, B> map(onLeft: (L) -> A, onRight: (R) -> B): Either<A, B> =
        when (this) {
            is Left -> Left(onLeft(this.value))
            is Right -> Right(onRight(this.value))
        }

    inline fun <A, B> flatMap(onLeft: (L) -> Either<A, B>, onRight: (R) -> Either<A, B>): Either<A, B> =
        when (this) {
            is Left -> onLeft(this.value)
            is Right -> onRight(this.value)
        }

    inline fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T =
        when (this) {
            is Left -> onLeft(this.value)
            is Right -> onRight(this.value)
        }

    data class Left<out L>(val value: L) : Either<L, Nothing>()

    data class Right<out R>(val value: R) : Either<Nothing, R>()

}

inline fun <L, R, T> Either<L, R>.flatMapLeft(mapper: (L) -> Either<T, R>): Either<T, R> =
    when (this) {
        is Either.Left -> mapper(this.value)
        is Either.Right -> this
    }

inline fun <L, R, T> Either<L, R>.flatMapRight(mapper: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> mapper(this.value)
    }
