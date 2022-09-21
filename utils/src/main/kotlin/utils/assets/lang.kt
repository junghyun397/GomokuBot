package utils.assets

import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max

fun Int.bound(): Int = max(0, this)

fun Int.toBytes(): ByteArray =
    ByteBuffer.allocate(4)
        .apply {
            putInt(this@toBytes)
        }
        .array()

fun List<Byte>.getFirstInt(): Int {
    var result = this[0].toInt() shl 12
    result = result or (this[1].toInt() shl 8)
    result = result or (this[2].toInt() shl 4)
    result = result or this[3].toInt()

    return result
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun <T : Enum<*>> T.toEnumString(): String = "${this.javaClass.simpleName}-$this"

fun <T: Comparable<T>> Iterable<T>.maxIndexes(): Array<Int>? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null

    val maxPoints = LinkedList<Int>()
    var max = iterator.next()

    maxPoints.add(0)

    var idx = 0
    while (iterator.hasNext()) {
        val e = iterator.next()
        idx += 1

        if (e > max) {
            maxPoints.clear()
            max = e
            maxPoints.add(idx)
        } else if (e == max) {
            maxPoints.add(idx)
        }
    }

    return maxPoints.toTypedArray()
}
