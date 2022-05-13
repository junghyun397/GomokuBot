package core.inference

import jrenju.Board
import jrenju.StructOps
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.assets.bound
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    private fun isStoneExist(board: Board, row: Int, col: Int) =
        row in 0 until Renju.BOARD_WIDTH() && col in 0 until Renju.BOARD_WIDTH()
                && Flag.isExist(board.boardField()[Pos.rowColToIdx(row, col)])

    private fun evaluateParticleWeight(board: Board, threeSideTrap: Boolean, fourSideTrap: Boolean, idx: Int): Int {
        val flag = board.boardField()[idx]
        val cond = board.pointsField()[idx]
        val color = board.color()

        var scoreBlack = 0.0F

        if (!Flag.isExist(flag)) {
            scoreBlack += if (cond.black().three() > 0 && cond.black().closedFour() > 0)
                RenjuWeights.THREE_FOUR_FORK
            else
                cond.black().three() * RenjuWeights.OPEN_THREE +
                        cond.black().openFour() * RenjuWeights.OPEN_FOUR +
                        cond.black().closedFour() * RenjuWeights.CLOSED_FOUR +
                        cond.black().fiveInRow() * RenjuWeights.FIVE
        }

        var scoreWhite = 0.0F

        scoreWhite += if (cond.white().four() > 1)
            RenjuWeights.DOUBLE_FOUR_FORK
        else if (cond.white().three() > 1)
            RenjuWeights.DOUBLE_THREE_FORK
        else {
            if (cond.white().three() > 0 && cond.white().closedFour() > 0)
                RenjuWeights.THREE_FOUR_FORK
            else
                cond.white().three() * RenjuWeights.OPEN_THREE +
                        cond.white().openFour() * RenjuWeights.OPEN_FOUR +
                        cond.white().closedFour() * RenjuWeights.CLOSED_FOUR +
                        cond.white().fiveInRow() * RenjuWeights.FIVE
        }

        scoreWhite += cond.white().fiveInRow() * RenjuWeights.FIVE

        when (color) {
            Color.BLACK() -> scoreBlack *= RenjuWeights.SELF_FACTOR
            Color.WHITE() -> scoreWhite *= RenjuWeights.SELF_FACTOR
        }

        var score = abs(scoreBlack + scoreWhite).toInt()

        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        if (
            Flag.onlyStone(flag) == Flag.FREE() &&
            (this.isStoneExist(board, row + 1, col - 1) || this.isStoneExist(board, row + 1, col) || this.isStoneExist(board, row + 1, col + 1) ||
            this.isStoneExist(board, row, col - 1) || this.isStoneExist(board, row, col + 1) ||
            this.isStoneExist(board, row - 1, col - 1) || this.isStoneExist(board, row - 1, col) || this.isStoneExist(board, row - 1, col + 1))
        )
            score += RenjuWeights.NEIGHBORHOOD_EXTRA

        if (!Flag.isForbid(flag)) {
            if (threeSideTrap)
                score += if (cond.white().three() > 0 || cond.white().four() > 0)
                    RenjuWeights.THREE_SIDE_TRAP_TREAT_FORK
                else RenjuWeights.THREE_SIDE_TRAP

            if (fourSideTrap)
                score += RenjuWeights.FOUR_SIDE_TRAP
        }
        return score
    }

    fun evaluateBoard(board: Board): MutableList<MutableList<Int>> =
        StructOps(board).collectTrapPoints().let { traps ->
            (0 until Renju.BOARD_SIZE())
                .map { this.evaluateParticleWeight(board, traps._1().contains(it), traps._2().contains(it), it) }
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

                evaluated[latestPos.row()][latestPos.col()] += RenjuWeights.LATEST_MOVE

                for (row in (latestPos.row() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelHalf))
                    for (col in (latestPos.col() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelHalf))
                        evaluated[row][col] += RenjuWeights.CENTER_EXTRA

                for (row in (latestPos.row() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelQuarter))
                    for (col in (latestPos.col() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelQuarter))
                        evaluated[row][col] += RenjuWeights.CENTER_EXTRA

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
