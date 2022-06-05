package core.inference

import jrenju.Board
import jrenju.StructOps
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.assets.bound
import utils.assets.toInt
import java.util.*
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    object FocusWeights : RenjuUtils.WeightSet() {

        const val latestMove = 500
        const val centerExtra = 1

        override val neighborhoodExtra = 2

        override val closedFour = 2
        override val openThree = 5

        override val threeSideTrap = 10
        override val fourSideTrap = 150

        override val doubleThreeFork = 30
        override val threeFourFork = 50
        override val doubleFourFork = 50

        override val blockFour = 100
        override val openFour = 150
        override val five = 200

    }

    fun evaluateBoard(board: Board): MutableList<MutableList<Int>> {
        val traps = StructOps(board).collectTrapPoints()

        return (0 until Renju.BOARD_SIZE())
            .map {
                val flag = board.boardField()[it]

                if (Flag.isExist(flag) || Flag.isForbid(flag, board.nextColorFlag()))
                    0
                else {
                    val particlePair = board.getParticlePair(it)

                    RenjuUtils.evaluateParticle(FocusWeights, particlePair.apply(board.nextColorFlag())) +
                            RenjuUtils.evaluateParticle(FocusWeights, particlePair.apply(board.colorFlag())) +
                            RenjuUtils.hasNeighborhood(board, it).toInt() * FocusWeights.neighborhoodExtra +
                            traps._1.contains(it).toInt() * FocusWeights.threeSideTrap +
                            traps._2.contains(it).toInt() * FocusWeights.fourSideTrap
                }
            }
            .chunked(Renju.BOARD_WIDTH()) { it.toMutableList() }
            .toMutableList()
    }

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: Board, kernelWidth: Int): Pos =
        board.latestPos().fold(
            { Renju.BOARD_CENTER_POS() },
            { latestPos ->
                val kernelHalf = kernelWidth / 2
                val kernelQuarter = kernelWidth / 4

                val evaluated = this.evaluateBoard(board)

                evaluated[latestPos.row()][latestPos.col()] += FocusWeights.latestMove

                for (row in (latestPos.row() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelHalf))
                    for (col in (latestPos.col() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelHalf))
                        evaluated[row][col] += FocusWeights.centerExtra

                for (row in (latestPos.row() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelQuarter))
                    for (col in (latestPos.col() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelQuarter))
                        evaluated[row][col] += FocusWeights.centerExtra

                val prefix = evaluated
                    .map { it.runningFold(0) { acc, weight -> acc + weight } }
                    .runningFold(Collections.nCopies(Renju.BOARD_WIDTH() + 1, 0)) { acc, col ->
                        acc.zip(col).map { it.first + it.second }
                    }
                    .toMutableList()

                val step = Renju.BOARD_WIDTH() - kernelWidth

                var max = Triple(0, latestPos.row(), latestPos.col())
                for (row in (0 .. step))
                    for (col in (0 .. step)) {
                        val collected = prefix[row + kernelWidth][col + kernelWidth] - prefix[row][col + kernelWidth] -
                                prefix[row + kernelWidth][col] + prefix[row][col]
                        if (collected > max.first) max = Triple(collected, row, col)
                    }

                Pos(max(kernelHalf, max.second + kernelHalf), max(kernelHalf, max.third + kernelHalf))
            }
        )

}
