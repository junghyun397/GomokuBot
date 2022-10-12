package utils.lang

infix fun <A, B> A.pair(that: B): Pair<A, B> =
    Pair(this, that)
