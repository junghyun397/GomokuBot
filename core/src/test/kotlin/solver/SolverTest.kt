package solver

import core.inference.FocusSolver
import core.inference.WeightSet
import jrenju.Board
import jrenju.`BoardIO$`
import jrenju.protocol.Solution
import org.junit.Test
import java.util.*
import java.util.concurrent.ForkJoinPool

internal class SolverTest {

    object TestWeight : WeightSet() {

        override val neighborhoodExtra = 2

        override val closedFour = 2
        override val openThree = 3

        override val blockThree = 0
        override val openFour = 150
        override val five = 200

        override val blockFourExtra = 100
        override val treatBlockThreeFork = 110

        override val threeSideTrap = 10
        override val fourSideTrap = 150
        override val treatThreeSideTrapFork = 50

        override val doubleThreeFork = 50
        override val threeFourFork = 105
        override val doubleFourFork = 150

    }

    @Test
    fun aiBenchmark() {
        var solver1Wins = 0
        var solver2Wins = 0
        var draws = 0

        ForkJoinPool.commonPool()

        for (i in 0 until 1000) {
            when (this.match(Board.newBoard(), { FocusSolver.findSolution(it) }, { FocusSolver.findSolution(it, TestWeight) })) {
                true -> solver1Wins += 1
                false -> solver2Wins += 1
                null -> draws += 1
            }
        }

        println("$solver1Wins $solver2Wins $draws")
    }

    private fun match(originalBoard: Board, solver1: (Board) -> Solution, solver2: (Board) -> Solution): Boolean? {
        var board = originalBoard

        val (blackSolver, whiteSolver) = run {
            if (Random().nextBoolean())
                solver1 to solver2
            else
                solver2 to solver1
        }

        @Suppress("UNREACHABLE_CODE") val result: Boolean? = run {
            while (true) {
                board = board.makeMove(whiteSolver(board).idx())

                if (board.winner().isDefined)
                    return@run whiteSolver == solver1

                if (board.moves() == 224)
                    return@run null

                board = board.makeMove(blackSolver(board).idx())

                if (board.winner().isDefined)
                    return@run blackSolver == solver1

                if (board.moves() == 224)
                    return@run null
            }

            throw Exception()
        }

        println(`BoardIO$`.`MODULE$`.BoardToText(board).boardText())

        return result
    }

}