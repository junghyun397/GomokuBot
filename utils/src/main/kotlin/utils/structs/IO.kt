@file:Suppress("unused")

package utils.structs

interface IO<out A> {

    suspend fun run(): A

    companion object {

        inline fun <A> unit(crossinline block: suspend () -> A) =
            object : IO<A> {
                override suspend fun run() = block()
            }

        inline operator fun <A> invoke(crossinline block: suspend () -> A) = unit(block)

        fun <A, B> zip(a: IO<A>, b: IO<B>) =
            object : IO<Pair<A, B>> {
                override suspend fun run() = a.run() to b.run()
            }

        fun <A, B, C> zip(a: IO<A>, b: IO<B>, c: IO<C>) =
            object : IO<Triple<A, B, C>> {
                override suspend fun run() = Triple(a.run(), b.run(), c.run())
            }

    }

}

inline fun <A, B> IO<A>.map(crossinline mapper: suspend (A) -> B): IO<B> =
    object : IO<B> {
        override suspend fun run() = mapper(this@map.run())
    }

fun <A, B> IO<A>.flatMap(mapper: suspend (A) -> IO<B>): IO<B> =
    object : IO<B> {
        override suspend fun run() = mapper(this@flatMap.run()).run()
    }
