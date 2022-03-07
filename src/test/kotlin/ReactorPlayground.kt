import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.mono
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

internal class ReactorPlayground {

    @Test
    fun testFlux() {
        (1..1000).map { "Num$it" }.toFlux()
            .flatMap { "oO$it".toMono() }
            .flatMap { event -> mono { delayExec{ "-$event ==!" } } }
            .subscribe { println(it) }
    }

    private suspend fun <O> delayExec(block: (Unit) -> O): O {
        delay(1)
        return block(Unit)
    }

}