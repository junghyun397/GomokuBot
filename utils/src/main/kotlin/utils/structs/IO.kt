@file:Suppress("unused")

package utils.structs

import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

interface IO<out A> {

    suspend fun run(): A

    companion object {

        val empty = object : IO<Unit> {
            override suspend fun run() = Unit
        }

        inline fun <A> unit(crossinline block: suspend () -> A) =
            object : IO<A> {
                override suspend fun run() = block()
            }

        inline fun effect(crossinline block: () -> Unit) =
            object : IO<Unit> {
                override suspend fun run() = block()
            }

        inline operator fun <A> invoke(crossinline block: suspend () -> A) = unit(block)

        fun <A, B> zip(a: IO<A>, b: IO<B>): IO<Pair<A, B>> =
            object : IO<Pair<A, B>> {
                override suspend fun run() = Pair(a.run(), b.run())
            }

        fun <A, B, C> zip(a: IO<A>, b: IO<B>, c: IO<C>): IO<Triple<A, B, C>> =
            object : IO<Triple<A, B, C>> {
                override suspend fun run() = Triple(a.run(), b.run(), c.run())
            }

        fun <A, B, C, D> zip(a: IO<A>, b: IO<B>, c: IO<C>, d: IO<D>): IO<Quadruple<A, B, C, D>> =
            object : IO<Quadruple<A, B, C, D>> {
                override suspend fun run() = Quadruple(a.run(), b.run(), c.run(), d.run())
            }

    }

}

fun <A, B> IO<A>.map(mapper: (A) -> B): IO<B> =
    object : IO<B> {
        override suspend fun run() = mapper(this@map.run())
    }

fun <A, B> IO<A>.flatMap(mapper: (A) -> IO<B>): IO<B> =
    object : IO<B> {
        override suspend fun run() = mapper(this@flatMap.run()).run()
    }

fun <A, B> IO<Option<A>>.mapOption(mapper: (A) -> B): IO<Option<B>> =
    object : IO<Option<B>> {
        override suspend fun run() = this@mapOption.run().map { mapper(it) }
    }

fun <A, B> IO<Option<A>>.flatMapOption(mapper: (A) -> IO<B>): IO<Option<B>> =
    object : IO<Option<B>> {
        override suspend fun run() = this@flatMapOption.run().map { mapper(it).run() }
    }

fun <A> IO<A>.toMono(): Mono<A> =
    mono { this@toMono.run() }
