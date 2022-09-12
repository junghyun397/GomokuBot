package utils.lang

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.time.Duration

fun <T> schedule(interval: Duration, job: suspend FlowCollector<T>.() -> Unit): Flow<T> {
    val intervalMillis = interval.toMillis()

    return flow {
        while (true) {
            delay(intervalMillis)
            job(this)
        }
    }
}
