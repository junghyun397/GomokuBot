package utils.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

fun schedule(interval: Duration, onError: (java.lang.Exception) -> Unit = { }, job: () -> Unit) =
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            delay(interval.toMillis())
            try {
                job()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
