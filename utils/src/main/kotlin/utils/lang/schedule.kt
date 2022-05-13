package utils.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

fun schedule(interval: Duration, job: () -> Unit) = CoroutineScope(Dispatchers.Default).launch {
    while (true) {
        delay(interval.toMillis())
        job()
    }
}
