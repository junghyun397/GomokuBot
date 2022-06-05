@file:Suppress("unused")

package utils.structs

interface IO<out A> {

    suspend fun run(): A

    fun <B> map(mapper: suspend (A) -> B): IO<B> =
        object : IO<B> {
            override suspend fun run() = mapper(this@IO.run())
        }

    fun <B> flatMap(mapper: suspend (A) -> IO<B>): IO<B> =
        object : IO<B> {
            override suspend fun run() = mapper(this@IO.run()).run()
        }

    companion object {

        fun <A> unit(block: suspend () -> A) =
            object : IO<A> {
                override suspend fun run() = block()
            }

        operator fun <A> invoke(block: suspend () -> A) = unit(block)

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
