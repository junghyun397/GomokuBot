package core.inference

import jrenju.Board
import jrenju.notation.Pos
import jrenju.protocol.Presets
import jrenju.protocol.Solution

val apePresets = Presets(10_000, 7, 10, 4)
val beginnerPresets = Presets(100_000, 9, 16, 8)
val moderatePresets = Presets(1_000_000, 11, 24, 16)
val expertPresets = Presets(10_000_000, 16, 24, 24)
val guruPresets = Presets(100_000_000, Int.MAX_VALUE, 32, 24)

enum class AiLevel(val solver: (KvineClient, Board, Pos) -> Solution) {
    AMOEBA(amoebaSolver),
    APE(kvineSolver(apePresets)),
    BEGINNER(kvineSolver(beginnerPresets)),
    MODERATE(kvineSolver(moderatePresets)),
    EXPERT(kvineSolver(expertPresets)),
    GURU(kvineSolver(guruPresets))
}

val amoebaSolver: (KvineClient, Board, Pos) -> Solution = { _, board, _ -> AmoebaSolver.solve(board) }

// currying
fun kvineSolver(aiPresets: Presets): (KvineClient, Board, Pos) -> Solution = { kvineClient, board, pos ->
    kvineSolver(aiPresets, kvineClient, board, pos)
}

private fun kvineSolver(aiPresets: Presets, kvineClient: KvineClient, board: Board, latestMove: Pos): Solution = TODO()
