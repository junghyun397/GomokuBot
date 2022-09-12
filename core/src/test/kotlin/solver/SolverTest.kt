@file:Suppress("UNREACHABLE_CODE")

package solver

import core.inference.FocusSolver
import jrenju.Board
import jrenju.`BoardIO$`
import jrenju.protocol.Solution
import org.junit.Test
import utils.lang.and
import java.util.*

internal class SolverTest {

    @Test
    fun aiBenchmark() {
        var solver1Wins = 0
        var solver2Wins = 0
        var draws = 0

        for (i in 0 until 1) {
            when (this.match(Board.newBoard(), { FocusSolver.findSolution(it) }, { FocusSolver.findSolution(it) })) {
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
                solver1 and solver2
            else
                solver2 and solver1
        }

        val result: Boolean? = run {
            while (true) {
                board = board.makeMove(whiteSolver(board).idx())

                if (board.winner().isDefined)
                    return@run whiteSolver == solver1

                if (board.moves() == 224)
                    return@run null

                board = board.makeMove(blackSolver(board).idx())

                if (board.winner().isDefined)
                    return@run blackSolver == solver1

                if (board.moves() == 221)
                    return@run null
            }

            return null
        }

        println(`BoardIO$`.`MODULE$`.BoardToText(board).boardText())

        return result
    }

}
