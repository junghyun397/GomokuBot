package utils

import java.util.concurrent.ConcurrentHashMap

fun <I: Any, O: Any> memoize(f: (I) -> O): (I) -> O {
    val cache = ConcurrentHashMap<I, O>()
    return { key ->
        cache[key]
            ?: f(key).also {
                cache[key] = it
            }
    }
}
