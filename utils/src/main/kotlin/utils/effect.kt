@file:Suppress("unused")

package utils

import arrow.core.raise.Effect
import arrow.core.raise.effect
import arrow.core.raise.get

fun <A, B> ioZip(a: Effect<Nothing, A>, b: Effect<Nothing, B>): Effect<Nothing, Pair<A, B>> = effect {
    Pair(a.get(), b.get())
}

fun <A, B, C> ioZip(a: Effect<Nothing, A>, b: Effect<Nothing, B>, c: Effect<Nothing, C>): Effect<Nothing, Triple<A, B, C>> =
    effect {
        Triple(a.get(), b.get(), c.get())
    }

fun <A, B, C, D> ioZip(a: Effect<Nothing, A>, b: Effect<Nothing, B>, c: Effect<Nothing, C>, d: Effect<Nothing, D>): Effect<Nothing, Quadruple<A, B, C, D>> =
    effect {
        Quadruple(a.get(), b.get(), c.get(), d.get())
    }
