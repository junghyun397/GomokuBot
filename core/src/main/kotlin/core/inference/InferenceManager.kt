package core.inference

import jrenju.AttackPoints
import jrenju.Board
import jrenju.L3Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.rule.Renju
import utils.assets.bound
import utils.structs.Option
import java.util.*
import kotlin.math.max
import kotlin.math.min

object InferenceManager {

    suspend fun requestSolution(client: B3nzeneClient, board: Board): Option<Pair<L3Board, Pos>> = TODO()

    suspend fun terminateSession(client: B3nzeneClient, board: Board): Unit = TODO()

    suspend fun reportSession(client: B3nzeneClient, board: Board): Unit = TODO()

    private fun evaluateWeight(flag: Byte, cond: AttackPoints): Int =
        if (cond.black3() > 0 && cond.blackC4() > 0) RenjuWeights.OVERLAP_THREE_FOUR else
            cond.black3() * RenjuWeights.OPEN_THREE + cond.blackC4() * RenjuWeights.CLOSED_FOUR +

        cond.black5() * RenjuWeights.FIVE +

        if (flag > Flag.FREE()) 0 else
            cond.black3() * RenjuWeights.OPEN_THREE + cond.blackC4() * RenjuWeights.CLOSED_FOUR +
                    cond.blackO4() * RenjuWeights.OPEN_FOUR +

        if (cond.white3() > 0 && cond.whiteC4() > 0) RenjuWeights.OVERLAP_THREE_FOUR else
            cond.white3() * RenjuWeights.OPEN_THREE + cond.whiteC4() * RenjuWeights.CLOSED_FOUR +

        cond.whiteO4() * RenjuWeights.OPEN_FOUR +
        cond.white5() * RenjuWeights.FIVE +

        if (cond.white3() > 1) RenjuWeights.DOUBLE_THREE else cond.white3() * RenjuWeights.OPEN_THREE +
        if (cond.whiteC4() > 1) RenjuWeights.DOUBLE_FOUR else cond.whiteC4() * RenjuWeights.CLOSED_FOUR

    private fun evaluateBoard(board: L3Board): MutableList<MutableList<Int>> =
        board.boardField().zip(board.attackField())
            .map { this.evaluateWeight(it.first, it.second) }
            .chunked(Renju.BOARD_WIDTH()) { it.toMutableList() }
            .toMutableList()

    fun resolveBoard(board: L3Board): Pos {
        val evaluated = this.evaluateBoard(board)
        TODO()
    }

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: L3Board, kernelWidth: Int): Pos {
        val kernelHalf = kernelWidth / 2

        val evaluated = this.evaluateBoard(board)

        val latestPos = board.latestPos()

        evaluated[latestPos.col()][latestPos.row()] += RenjuWeights.LATEST_MOVE

        for (col in (latestPos.col() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH() - 1, latestPos.col() + kernelHalf))
            for (row in (latestPos.row() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH() - 1, latestPos.row() + kernelHalf))
                evaluated[col][row] += RenjuWeights.CENTER_EXTRA

        val sum = evaluated
            .map { it.runningFold(0) { acc, weight -> acc + weight }.drop(1) }
            .runningFold(Collections.nCopies(Renju.BOARD_WIDTH(), 0)) { acc, row ->
                acc.zip(row).map { it.first + it.second }
            }.drop(1)
            .toMutableList()

        val step = Renju.BOARD_WIDTH() - kernelWidth

        var max = 0 to (latestPos.col() to latestPos.row())
        for (col in (0 .. step))
            for (row in (0 .. step)) {
                val collected = sum[col + kernelWidth - 1][row + kernelWidth - 1] - sum[(col - 1).bound()][row + kernelWidth - 1] -
                        sum[col + kernelWidth - 1][(row - 1).bound()] + sum[(col - 1).bound()][(row - 1).bound()]
                if (collected > max.first) max = collected to (col to row)
            }

        return max.second.let { Pos(
            max(kernelHalf, it.second + kernelHalf),
            max(kernelHalf, it.first + kernelHalf)
        ) }
    }

}
