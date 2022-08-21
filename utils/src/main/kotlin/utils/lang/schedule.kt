package utils.lang

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Duration

fun schedule(interval: Duration, job: suspend () -> Unit) =
    flow {
        while (true) {
            delay(interval.toMillis())
            job()
            emit(Unit)
        }
    }
