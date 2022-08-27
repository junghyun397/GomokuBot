package utils.lang

import utils.structs.Quadruple

infix fun <A, B> A.and(that: B): Pair<A, B> =
    Pair(this, that)

infix fun <A, B, C> Pair<A, B>.and(that: C): Triple<A, B, C> =
    Triple(this.first, this.second, that)

infix fun <A, B, C, D> Triple<A, B, C>.and(that: D): Quadruple<A, B, C, D> =
    Quadruple(this.first, this.second, this.third, that)
