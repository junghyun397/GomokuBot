package utils

import jrenju.Board
import jrenju.rule.Renju
import utils.assets.bound
import java.util.*
import kotlin.test.Test

internal class OptionTest {

    @Test
    fun chunked() {
        val board = Board.newBoard()
            .calculateL2Board()
            .calculateL3Board()

        println(board.attackField().map { 5 }
            .chunked(Renju.BOARD_WIDTH()))

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
            .runningFold(Collections.nCopies(sum.first().size, 0)) { acc, row ->
                acc.zip(row).map { it.first + it.second }
            }.drop(1)

        mid.forEach { println(it) }
        println("--")

        val kernelSize = 2
        val step = kernelSize - 1

        var max = 0 to (0 to 0)
        for (col in (0 .. source.size - kernelSize)) {
            for (row in (0 .. source.first().size - kernelSize)) {
                val collected = mid[col + step][row + step] - mid[(col - 1).bound()][row + step] -
                        mid[col + step][(row - 1).bound()] + mid[(col - 1).bound()][(row - 1).bound()]
                if (collected > max.first) max = collected to (col to row)
            }
        }

        println(max)
    }

}