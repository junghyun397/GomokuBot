package core.inference

import jrenju.Board
import jrenju.StructOps
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Renju
import jrenju.protocol.Solution
import jrenju.protocol.SolutionLeaf
import jrenju.solve.LRUMemo
import jrenju.solve.`SolutionMapper$`
import jrenju.solve.`VCFSolver$`
import utils.assets.maxSet
import utils.assets.toInt
import java.util.*

object AmoebaSolver {

    object AmoebaWeights : RenjuUtils.WeightSet() {

        override val neighborhoodExtra = 1

        override val closedFour = 3
        override val openThree = 10

        override val threeSideTrap = 10
        override val fourSideTrap = 10

        override val doubleThreeFork = 10
        override val threeFourFork = 10
        override val doubleFourFork = 10

        override val openFour = 3
        override val blockFour = 3
        override val five = 3

    }

    private const val LARGE_INT = Int.MAX_VALUE - 100

    private fun evaluateMove(board: Board, move: Int): Int {
        val flag = board.boardField()[move]
        val isForbid = Flag.isForbid(flag, board.nextColorFlag())
        val topParticlePair = board.getParticlePair(move)

        if (isForbid)
            return Int.MIN_VALUE

        if (topParticlePair.apply(board.nextColorFlag()).fiveTotal() > 0)
            return LARGE_INT

        if (topParticlePair.apply(board.colorFlag()).fiveTotal() > 0)
            return LARGE_INT

        val evalMine = RenjuUtils.evaluateParticle(FocusSolver.FocusWeights, topParticlePair.apply(board.nextColorFlag()))
        val evalOpponent = RenjuUtils.evaluateParticle(FocusSolver.FocusWeights, topParticlePair.apply(board.colorFlag()))

        var baseScore = run {
            if (evalMine + evalOpponent > 50)
                if (evalMine > evalOpponent)
                    evalMine * 2000
                else
                    evalOpponent * 1000
            else 0
        }

        val thenBoard = board.makeMove(move)

        for (idx in 0 until Renju.BOARD_SIZE()) {
            val particlePair = thenBoard.getParticlePair(idx)

            if (board.nextColor() == Color.WHITE() || particlePair.forbidKind().isEmpty)
                baseScore += RenjuUtils.evaluateParticle(AmoebaWeights, particlePair.apply(board.nextColorFlag())) -
                        RenjuUtils.evaluateParticle(AmoebaWeights, particlePair.apply(board.colorFlag()))
        }

        return baseScore
    }

    fun solve(board: Board): Solution {
        val traps = StructOps(board).collectTrapPoints()

        val eval = (0 until Renju.BOARD_SIZE()).map {
            val flag = board.boardField()[it]

            if (Flag.isEmpty(flag) && !Flag.isForbid(flag, board.nextColorFlag())) {
                val baseScore = run {
                    if (board.moves() < 5 && RenjuUtils.hasNeighborhood(board, it))
                        FocusSolver.FocusWeights.neighborhoodExtra
                    else 0
                }

                baseScore +
                        traps._1.contains(it).toInt() * FocusSolver.FocusWeights.threeSideTrap +
                        traps._2.contains(it).toInt() * FocusSolver.FocusWeights.fourSideTrap +
                        evaluateMove(board, it)
            } else
                Int.MIN_VALUE
        }

        val max = eval.maxSet()!!

        val vcfSequence = if (max.first() < LARGE_INT)
            `VCFSolver$`.`MODULE$`.VCFFinder(board).findVCFSequence(LRUMemo.empty(), Int.MAX_VALUE)
                .let { if (it.size() == 0) null else it }
        else null

        return if (vcfSequence != null)
            `SolutionMapper$`.`MODULE$`.SequenceToNode(vcfSequence).toSolution()
        else
            SolutionLeaf(max[Random().nextInt(max.size)])
    }

    fun focusSolve(board: Board): Solution {
        val eval = FocusSolver.evaluateBoard(board).flatten()
        
        val max = eval.maxSet()!!

        val vcfSequence = if (max.first() < 200)
            `VCFSolver$`.`MODULE$`.VCFFinder(board).findVCFSequence(LRUMemo.empty(), Int.MAX_VALUE)
                .let { if (it.size() == 0) null else it }
        else null

        return if (vcfSequence != null)
            `SolutionMapper$`.`MODULE$`.SequenceToNode(vcfSequence).toSolution()
        else
            SolutionLeaf(max[Random().nextInt(max.size)])
    }

}
