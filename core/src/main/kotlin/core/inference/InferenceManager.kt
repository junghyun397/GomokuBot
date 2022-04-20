package core.inference

import jrenju.Board
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import jrenju.protocol.Solution
import utils.assets.bound
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object InferenceManager {

    private fun isStoneExist(board: Board, row: Int, col: Int) =
        Flag.onlyStone(board.boardField()[Pos.rowColToIdx(row, col).coerceIn(0, Renju.BOARD_LENGTH() - 1)]) != Flag.FREE()

    private fun evaluateWeight(board: Board, idx: Int): Int {
        val flag = board.boardField()[idx]
        val cond = board.pointsField()[idx]
        val color = board.color()

        var scoreBlack = 0.0F

        if (flag == Flag.FREE()) {
            scoreBlack += if (cond.black().three() > 0 && cond.black().closed4().sum() > 0)
                RenjuWeights.THREE_FOUR_FORK
            else
                cond.black().three() * RenjuWeights.OPEN_THREE +
                        cond.black().open4().count { it } * RenjuWeights.OPEN_FOUR +
                        cond.black().closed4().sum() * RenjuWeights.CLOSED_FOUR +
                        cond.black().fiveInRow() * RenjuWeights.FIVE
        }

        var scoreWhite = 0.0F

        scoreWhite += if (cond.white().four() > 1)
            RenjuWeights.DOUBLE_FOUR_FORK
        else if (cond.white().three() > 1)
            RenjuWeights.DOUBLE_THREE_FORK
        else {
            if (cond.white().three() > 0 && cond.white().closed4().sum() > 0)
                RenjuWeights.THREE_FOUR_FORK
            else
                cond.white().three() * RenjuWeights.OPEN_THREE +
                        cond.white().open4().count { it } * RenjuWeights.OPEN_FOUR +
                        cond.white().closed4().sum() * RenjuWeights.CLOSED_FOUR
        }

        scoreWhite += cond.white().fiveInRow() * RenjuWeights.FIVE

        when (color) {
            Color.BLACK() -> scoreBlack *= RenjuWeights.SELF_FACTOR
            Color.WHITE() -> scoreWhite *= RenjuWeights.SELF_FACTOR
        }

        var score = abs(scoreBlack + scoreWhite).toInt()

        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        if (Flag.onlyStone(flag) == Flag.FREE() &&
            (this.isStoneExist(board, row + 1, col - 1) || this.isStoneExist(board, row + 1, col) || this.isStoneExist(board, row + 1, col + 1) ||
            this.isStoneExist(board, row, col - 1) || this.isStoneExist(board, row, col + 1) ||
            this.isStoneExist(board, row - 1, col - 1) || this.isStoneExist(board, row - 1, col) || this.isStoneExist(board, row - 1, col + 1)))
            score += RenjuWeights.NEIGHBORHOOD_EXTRA

        return score
    }

    private fun evaluateBoard(board: Board): MutableList<MutableList<Int>> =
        (0 until Renju.BOARD_LENGTH())
            .map { this.evaluateWeight(board, it) }
            .chunked(Renju.BOARD_WIDTH()) { it.toMutableList() }
            .toMutableList()

    fun retrieveAiMove(board: Board, latestMove: Pos): Solution = TODO()

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: Board, kernelWidth: Int): Pos =
        board.latestPos().fold(
            { Renju.BOARD_CENTER() },
            { latestPos ->
                val kernelHalf = kernelWidth / 2
                val kernelQuarter = kernelHalf / 2

                val evaluated = this.evaluateBoard(board)

                evaluated[latestPos.row()][latestPos.col()] += RenjuWeights.LATEST_MOVE

                for (row in (latestPos.row() - kernelHalf).bound() .. min(Renju.BOARD_MAX_IDX(), latestPos.row() + kernelHalf))
                    for (col in (latestPos.col() - kernelHalf).bound() .. min(Renju.BOARD_MAX_IDX(), latestPos.col() + kernelHalf))
                        evaluated[row][col] += RenjuWeights.CENTER_EXTRA

                for (row in (latestPos.row() - kernelQuarter).bound() .. min(Renju.BOARD_MAX_IDX(), latestPos.row() + kernelQuarter))
                    for (col in (latestPos.col() - kernelQuarter).bound() .. min(Renju.BOARD_MAX_IDX(), latestPos.col() + kernelQuarter))
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
