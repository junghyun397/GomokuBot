package utils.lang

infix fun <A, B> A.pair(that: B): Pair<A, B> =
    Pair(this, that)

inline fun <T> T.shift(condition: Boolean, produce: (T) -> T): T =
    if (condition)
        produce(this)
    else
        this
