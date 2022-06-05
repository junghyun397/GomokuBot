package core.inference

import jrenju.Board
import jrenju.ParticleOps
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju

object RenjuUtils {

    abstract class WeightSet {

        abstract val neighborhoodExtra: Int

        abstract val closedFour: Int
        abstract val openThree: Int

        abstract val threeSideTrap: Int
        abstract val fourSideTrap: Int

        abstract val doubleThreeFork: Int
        abstract val threeFourFork: Int
        abstract val doubleFourFork: Int

        abstract val openFour: Int
        abstract val blockFour: Int
        abstract val five: Int

    }

    private fun isStoneExist(board: Board, row: Int, col: Int) =
        row in 0 until Renju.BOARD_WIDTH() && col in 0 until Renju.BOARD_WIDTH()
                && Flag.isExist(board.boardField()[Pos.rowColToIdx(row, col)])

    fun hasNeighborhood(board: Board, idx: Int): Boolean {
        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        return (isStoneExist(board, row + 1, col - 1) || isStoneExist(board, row + 1, col) || isStoneExist(board, row + 1, col + 1) ||
                isStoneExist(board, row, col - 1) || isStoneExist(board, row, col + 1) ||
                isStoneExist(board, row - 1, col - 1) || isStoneExist(board, row - 1, col) || isStoneExist(board, row - 1, col + 1))
    }

    fun evaluateParticle(weightSet: WeightSet, particle: ParticleOps): Int =
        if (particle.fourTotal() > 1)
            weightSet.doubleFourFork
        else if (particle.threeTotal() > 0 && particle.fourTotal() > 0)
            weightSet.threeFourFork
        else if (particle.threeTotal() > 1)
            weightSet.doubleThreeFork
        else
            particle.threeTotal() * weightSet.openThree +
                    particle.closedFourTotal() * weightSet.closedFour +
                    particle.openFourTotal() * weightSet.openFour +
                    particle.fiveTotal() * weightSet.five

}
