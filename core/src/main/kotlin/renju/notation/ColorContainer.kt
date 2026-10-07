package renju.notation

data class ColorContainer<out T>(val black: T, val white: T) {

    operator fun get(key: Color): T =
        when (key) {
            Color.BLACK -> this.black
            Color.WHITE -> this.white
        }

    fun color(key: Any?): Color? =
        when (key) {
            this.black -> Color.BLACK
            this.white -> Color.WHITE
            else -> null
        }

    fun swap(): ColorContainer<T> = ColorContainer(this.white, this.black)

    companion object {

        fun<T> new(black: T, white: T): ColorContainer<T> =
            ColorContainer(black, white)

    }

}

fun<T> ColorContainer<T>.setColor(key: Color, value: T): ColorContainer<T> =
    when (key) {
        Color.BLACK -> this.copy(black = value)
        Color.WHITE -> this.copy(white = value)
    }

fun<T> ColorContainer<T>.asList(): List<T> =
    listOf(this.black, this.white)

inline fun<T, R> ColorContainer<T>.map(transform: (T) -> R): ColorContainer<R> =
    ColorContainer(transform(this.black), transform(this.white))

inline fun<T> ColorContainer<T>.forEach(action: (T) -> Unit) {
    action(this.black)
    action(this.white)
}
