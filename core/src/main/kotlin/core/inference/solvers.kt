package core.inference

import jrenju.Board
import jrenju.notation.Pos
import jrenju.notation.Renju
import jrenju.protocol.Presets
import jrenju.protocol.Solution
import jrenju.protocol.SolutionLeaf
import jrenju.protocol.`SolutionNode$`
import jrenju.solve.LRUMemo
import jrenju.solve.`VCFSolver$`
import java.util.*

val beginnerPresets = Presets(100_000, 9, 16, 8)
val moderatePresets = Presets(1_000_000, 11, 24, 16)
val expertPresets = Presets(10_000_000, 16, 24, 24)
val guruPresets = Presets(100_000_000, Int.MAX_VALUE, 32, 24)

enum class AiLevel(val solver: (B3nzeneClient, Board, Pos) -> Solution) {
    AMOEBA(::surfaceSolver),
    APE(::shallowMiniMaxSolver),
    BEGINNER(b3nzeneSolver(beginnerPresets)),
    MODERATE(b3nzeneSolver(moderatePresets)),
    EXPERT(b3nzeneSolver(expertPresets)),
    GURU(b3nzeneSolver(guruPresets))
}

@Suppress("UNUSED_PARAMETER")
fun surfaceSolver(b3nzeneClient: B3nzeneClient, board: Board, latestMove: Pos): Solution {
    val eval = FocusSolver.evaluateBoard(board).flatMapIndexed { rowIdx: Int, row: MutableList<Int> ->
        row.mapIndexed { colIdx, score -> Pos.rowColToIdx(rowIdx, colIdx) to score }
    }

    val builder = MutableList(0) { 0 }
    var max = 0

    for (idx in 0 until Renju.BOARD_SIZE()) {
        if (eval[idx].second > max) {
            builder.clear()
            max = eval[idx].second
            builder.add(idx)
        } else if (eval[idx].second == max) {
            builder.add(idx)
        }
    }

    val vcfSequence = if (max < 200)
        `VCFSolver$`.`MODULE$`.VCFFinder(board).findVCFSequence(LRUMemo.empty(), Int.MAX_VALUE)
            .let { if (it.size() == 0) null else it }
    else null

    println(vcfSequence) // TODO

    return if (vcfSequence != null)
        `SolutionNode$`.`MODULE$`.SequenceToNode(vcfSequence).toSolution()
    else
        SolutionLeaf(builder[Random().nextInt(builder.size)])
}

@Suppress("UNUSED_PARAMETER")
fun shallowMiniMaxSolver(b3nzeneClient: B3nzeneClient, board: Board, latestMove: Pos): Solution = TODO()

// currying
fun b3nzeneSolver(aiPresets: Presets): (B3nzeneClient, Board, Pos) -> Solution = { b3nzeneClient, board, pos ->
    b3nzeneSolver(aiPresets, b3nzeneClient, board, pos)
}

private fun b3nzeneSolver(aiPresets: Presets, b3nzeneClient: B3nzeneClient, board: Board, latestMove: Pos): Solution = TODO()
