@file:Suppress("unused")

package utils.structs

sealed class Either<out L, out R> {

    val isLeft: Boolean get() = this is Left

    val isRight: Boolean get() = this is Right

    data class Left<out L>(val value: L) : Either<L, Nothing>()

    data class Right<out R>(val value: R) : Either<Nothing, R>()

}

inline fun <L, R, A, B> Either<L, R>.map(onLeft: (L) -> A, onRight: (R) -> B): Either<A, B> =
    when (this) {
        is Either.Left -> Either.Left(onLeft(this.value))
        is Either.Right -> Either.Right(onRight(this.value))
    }

inline fun <L, R, T> Either<L, R>.mapLeft(mapper: (L) -> T): Either<T, R> =
    when (this) {
        is Either.Left -> Either.Left(mapper(this.value))
        is Either.Right -> this
    }

inline fun <L, R, T> Either<L, R>.mapRight(mapper: (R) -> T): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> Either.Right(mapper(this.value))
    }

inline fun <L, R, A, B> Either<L, R>.flatMap(onLeft: (L) -> Either<A, B>, onRight: (R) -> Either<A, B>): Either<A, B> =
    when (this) {
        is Either.Left -> onLeft(this.value)
        is Either.Right -> onRight(this.value)
    }

inline fun <L, R, T> Either<L, R>.fold(onLeft: (L) -> T, onRight: (R) -> T): T =
    when (this) {
        is Either.Left -> onLeft(this.value)
        is Either.Right -> onRight(this.value)
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
