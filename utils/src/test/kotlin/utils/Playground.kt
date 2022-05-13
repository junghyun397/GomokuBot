package utils

import utils.assets.bound
import java.util.*
import kotlin.test.Test

internal class OptionTest {

    data class NS(var d: String?)

    fun add(map: MutableMap<String, NS>, map2: MutableMap<String, String>) {
        val nss = NS("ho!")

        map["key"] = nss

        map2[nss.d!!] = "val"

        nss.d = null
    }

    @Test
    fun weakHashMap() {
        val l = listOf("A", "B", "C")
        println(l.reduce { acc, s -> acc + s })
    }

    @Test
    fun format() {
        val n = 9
        println("%2d".format(n))
    }

    @Test
    fun prefixSum() {
        val source = listOf(
            listOf(0, 0, 0, 0, 0),
            listOf(0, 0, 4, 0, 0),
            listOf(0, 8, 0, 1, 1),
            listOf(0, 6, 0, 1, 2),
        )

        val sum  = source
            .map { it.runningFold(0) { acc, weight -> acc + weight }.drop(1) }

        sum.forEach { println(it) }
        println("--")

        val mid = sum
            .runningFold(Collections.nCopies(sum.first().size, 0)) { acc, col ->
                acc.zip(col).map { it.first + it.second }
            }.drop(1)

        mid.forEach { println(it) }
        println("--")

        val kernelSize = 2
        val step = kernelSize - 1

        var max = 0 to (0 to 0)
        for (row in (0 .. source.size - kernelSize)) {
            for (col in (0 .. source.first().size - kernelSize)) {
                val collected = mid[row + step][col + step] - mid[(row - 1).bound()][col + step] -
                        mid[row + step][(col - 1).bound()] + mid[(row - 1).bound()][(col - 1).bound()]
                if (collected > max.first) max = collected to (col to col)
            }
        }

        println(max)
    }

}