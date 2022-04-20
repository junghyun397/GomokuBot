package utils.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun schedule(interval: Long, job: () -> Unit) = CoroutineScope(Dispatchers.Main).launch {
    while (true) {
        delay(interval)
        job()
    }
}
