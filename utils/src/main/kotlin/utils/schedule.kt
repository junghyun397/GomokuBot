package utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

inline fun <T> schedule(
    interval: Duration,
    crossinline job: suspend FlowCollector<T>.() -> Unit,
    crossinline errorHandler: (Exception) -> Unit = { }
): Flow<T> {
    return flow {
        while (true) {
            delay(interval)
            try {
                job(this)
            } catch (error : Exception) {
                errorHandler(error)
            }
        }
    }
}
