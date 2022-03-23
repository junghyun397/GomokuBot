package utility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@JvmInline
value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {
    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()
}

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java.simpleName)
