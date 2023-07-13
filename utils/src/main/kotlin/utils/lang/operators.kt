package utils.lang

import utils.structs.Quadruple

inline fun <T> T.shift(condition: Boolean, produce: (T) -> T): T =
    if (condition)
        produce(this)
    else
        this

fun <A, B> tuple(a: A, b: B): Pair<A, B> =
    Pair(a, b)

fun <A, B, C> tuple(a: A, b: B, c: C): Triple<A, B, C> =
    Triple(a, b, c)

fun <A, B, C, D> tuple(a: A, b: B, c: C, d: D): Quadruple<A, B, C, D> =
    Quadruple(a, b, c, d)
