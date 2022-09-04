package utils.lang

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.time.Duration

fun <T> schedule(interval: Duration, job: suspend FlowCollector<T>.() -> Unit) =
    flow {
        while (true) {
            delay(interval.toMillis())
            job(this)
        }
    }
