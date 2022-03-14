package utility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@JvmInline
value class LinuxTime(val timestamp: Long)

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java.simpleName)
