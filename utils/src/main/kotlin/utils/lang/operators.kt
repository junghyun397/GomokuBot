package utils.lang

infix fun <A, B> A.and(that: B): Pair<A, B> =
    Pair(this, that)
