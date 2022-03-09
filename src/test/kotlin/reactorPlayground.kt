import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.mono
import reactor.kotlin.core.publisher.toFlux

val testFlux = (1..10).map { "Num$it" }.toFlux()

fun testFlux() {
    testFlux
        .flatMap { mono {
            delay(2000)
            it
        } }
        .filter { it == "Num9" }
        .doOnNext { println(it) }
        .doOnDiscard(String::class.java) { println("$it filtered") }
        .subscribe()
}

fun main() {
    testFlux()
    println("start")
    runBlocking {
        delay(10000)
    }
}
