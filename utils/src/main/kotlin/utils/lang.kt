@file:Suppress("unused")

package utils

import java.nio.ByteBuffer

inline fun <T> T.replaceIf(condition: Boolean, produce: (T) -> T): T =
    if (condition)
        produce(this)
    else
        this

fun Int.toBytes(): ByteArray =
    ByteBuffer.allocate(4)
        .apply {
            putInt(this@toBytes)
        }
        .array()

fun <T : Enum<*>> T.toEnumString(): String = "${this.javaClass.simpleName}-$this"

inline fun <T> orNull(cond: Boolean, produce: () -> T?): T? =
    if (cond) produce()
    else null
