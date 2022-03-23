package utility

sealed class Option<out T> {

    val isDefined: Boolean get() = this is Some

    val isEmpty: Boolean get() = this is Empty

    fun getOrNull(): T? = (this as Some).value

    inline fun <R> fold(onDefined: (T) -> R, onEmpty: () -> R): R =
        when(this) {
            is Some -> onDefined(value)
            is Empty -> onEmpty()
        }

    data class Some<out T>(val value: T) : Option<T>()

    object Empty : Option<Nothing>()

}

sealed class Either<out L, out R> {

    val isLeft: Boolean get() = this is Left

    val isRight: Boolean get() = this is Right

    inline fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T =
        when (this) {
            is Left -> onLeft(this.value)
            is Right -> onRight(this.value)
        }

    data class Left<out L>(val value: L) : Either<L, Nothing>()

    data class Right<out R>(val value: R) : Either<Nothing, R>()

}
