package core.inference

import jrenju.Board
import jrenju.notation.Pos
import jrenju.protocol.Presets
import jrenju.protocol.Solution
import utils.structs.Identifiable

val apePresets = Presets(10_000, 7, 10, 4)
val beginnerPresets = Presets(100_000, 9, 16, 8)
val moderatePresets = Presets(1_000_000, 11, 24, 16)
val expertPresets = Presets(10_000_000, 16, 24, 24)
val guruPresets = Presets(100_000_000, Int.MAX_VALUE, 32, 24)

enum class AiLevel(
    override val id: Short,
    val solver: (KvineClient, Board, Pos) -> Solution
) : Identifiable {
    AMOEBA(0, amoebaSolver),
    APE(1, kvineSolver(apePresets)),
    BEGINNER(2, kvineSolver(beginnerPresets)),
    MODERATE(3, kvineSolver(moderatePresets)),
    EXPERT(4, kvineSolver(expertPresets)),
    GURU(5, kvineSolver(guruPresets))
}

val amoebaSolver: (KvineClient, Board, Pos) -> Solution = { _, board, _ -> FocusSolver.findSolution(board) }

// currying
fun kvineSolver(aiPresets: Presets): (KvineClient, Board, Pos) -> Solution = { kvineClient, board, pos ->
    kvineSolver(aiPresets, kvineClient, board, pos)
}

private fun kvineSolver(aiPresets: Presets, kvineClient: KvineClient, board: Board, latestMove: Pos): Solution = TODO()
