package utils.assets

import java.util.*
import kotlin.math.max

fun Int.bound() = max(0, this)

fun Boolean.toInt() = if (this) 1 else 0

fun <T : Enum<*>> T.toEnumString() = "${this.javaClass.simpleName}-$this"

inline fun <reified T: Comparable<T>> Iterable<T>.maxSet(): Array<Int>? {
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

fun <T> Array<T>.choiceOne(): T =
    this[Random().nextInt(this.size)]
