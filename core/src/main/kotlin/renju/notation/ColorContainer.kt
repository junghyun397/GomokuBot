package renju.notation

data class ColorContainer<out T>(val black: T, val white: T) {

    operator fun get(key: Color): T =
        when (key) {
            Color.Black -> this.black
            Color.White -> this.white
        }

    fun color(key: Any?): Color? =
        when (key) {
            this.black -> Color.Black
            this.white -> Color.White
            else -> null
        }

    inline fun<R> map(transform: (T) -> R): ColorContainer<R> =
        ColorContainer(transform(this.black), transform(this.white))

    fun swap(): ColorContainer<T> = ColorContainer(this.white, this.black)

}
