package utils.lang

import java.util.concurrent.ConcurrentHashMap

fun <I, O> memoize(f: (I) -> O): (I) -> O {
    val cache = ConcurrentHashMap<I, O>()
    return { key ->
        cache[key]
            ?: f(key).also {
                cache[key] = it
            }
    }
}
