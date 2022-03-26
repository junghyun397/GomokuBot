@file:Suppress("unused")

package utils.monads

interface IO<out A> {

    fun run(): A

    fun <B> map(mapper: (A) -> B): IO<B> =
        object : IO<B> {
            override fun run() = mapper(this@IO.run())
        }

    fun <B> flatMap(mapper: (A) -> IO<B>): IO<B> =
        object : IO<B> {
            override fun run() = mapper(this@IO.run()).run()
        }

    companion object {

        fun <A> unit(block: () -> A) =
            object : IO<A> {
                override fun run() = block()
            }

        operator fun <A> invoke(block: () -> A) = unit(block)

    }

}
