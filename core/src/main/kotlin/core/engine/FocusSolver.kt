package core.engine

import renju.GameState
import renju.native.RustyRenjuCApi
import renju.notation.Pos
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    data class FocusInfo(val focus: Pos, val highlights: List<Pos>?)

    private fun isLegalEmptyMove(state: GameState, pos: Pos): Boolean =
        state.board.validateMove(pos) == null

    private fun evaluateBoard(state: GameState): MutableList<MutableList<Int>> {
        val nextColor = state.board.playerColor
        val opponentColor = !state.board.playerColor
        val masks = RustyRenjuCApi.constants

        fun Int.count(mask: Int): Int = (this and mask).countOneBits()
        fun Int.openFours(): Int = count(masks.openFourMask)
        fun Int.closeThrees(): Int = count(masks.closeThreeMask)
        fun Int.threes(): Int = count(masks.openThreeMask) + closeThrees()
        fun Int.fours(): Int = count(masks.closedFourMask) + openFours()

        fun Int.forkScore(): Int = when {
            fours() > 1 -> FocusWeights.DOUBLE_FOUR_FORK
            threes() > 0 && fours() > 0 -> FocusWeights.THREE_FOUR_FORK
            threes() > 1 -> FocusWeights.DOUBLE_THREE_FORK
            else -> 0
        }

        fun Int.baseScore(): Int = forkScore() +
                threes() * FocusWeights.OPEN_THREE +
                count(masks.closedFourMask) * FocusWeights.CLOSED_FOUR +
                count(masks.fiveMask) * FocusWeights.FIVE +
                count(masks.potentialMask) * FocusWeights.POTENTIAL

        fun Int.blockScore(other: Int): Int = when {
            closeThrees() == 0 -> 0
            other.threes() > 0 || other.fours() > 0 -> FocusWeights.TREAT_BLOCK_THREE_FORK
            else -> FocusWeights.BLOCK_THREE + openFours() * FocusWeights.BLOCK_FOUR_EXTRA
        }

        return (0 until Pos.BOARD_SIZE)
            .map(Pos::fromIdx)
            .map { pos ->
                if (!this.isLegalEmptyMove(state, pos)) {
                    0
                } else {
                    val self = state.board.pattern(pos, nextColor)
                    val opponent = state.board.pattern(pos, opponentColor)

                    self.baseScore() +
                            self.openFours() * FocusWeights.OPEN_FOUR +
                            opponent.baseScore() +
                            opponent.blockScore(self)
                }
            }
            .chunked(Pos.BOARD_WIDTH) { row -> row.toMutableList() }
            .toMutableList()
    }

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(state: GameState, windowWidth: Int, buildHighlights: Boolean): FocusInfo =
        state.history.lastOrNull()?.let { lastPos ->
            val boardWidth = Pos.BOARD_WIDTH
            val boardMaxIdx = Pos.BOARD_BOUND
            val clampedWindowWidth = windowWidth.coerceIn(1, boardWidth)
            val windowHalf = clampedWindowWidth / 2
            val windowQuarter = clampedWindowWidth / 4

            val evaluated = this.evaluateBoard(state)
            val highlights = if (buildHighlights) state.board.winningSequence() else null

            evaluated[lastPos.row][lastPos.col] += FocusWeights.LAST_MOVE

            for (row in max(0, lastPos.row - windowHalf)..min(boardMaxIdx, lastPos.row + windowHalf)) {
                for (col in max(0, lastPos.col - windowHalf)..min(boardMaxIdx, lastPos.col + windowHalf)) {
                    evaluated[row][col] += FocusWeights.CENTER_EXTRA
                }
            }

            if (state.history.size < 5) {
                for (row in max(0, lastPos.row - windowQuarter)..min(boardMaxIdx, lastPos.row + windowQuarter)) {
                    for (col in max(0, lastPos.col - windowQuarter)..min(boardMaxIdx, lastPos.col + windowQuarter)) {
                        evaluated[row][col] += FocusWeights.CENTER_EXTRA
                    }
                }
            }

            val prefix = Array(boardWidth + 1) { IntArray(boardWidth + 1) }

            for (row in 1..boardWidth) {
                for (col in 1..boardWidth) {
                    prefix[row][col] = evaluated[row - 1][col - 1] +
                            prefix[row - 1][col] +
                            prefix[row][col - 1] -
                            prefix[row - 1][col - 1]
                }
            }

            val step = boardWidth - clampedWindowWidth

            var maxScore = Int.MIN_VALUE
            var maxRow = lastPos.row.coerceIn(0, step)
            var maxCol = lastPos.col.coerceIn(0, step)

            for (row in 0..step) {
                for (col in 0..step) {
                    val collected = prefix[row + clampedWindowWidth][col + clampedWindowWidth] -
                            prefix[row][col + clampedWindowWidth] -
                            prefix[row + clampedWindowWidth][col] +
                            prefix[row][col]

                    if (collected > maxScore) {
                        maxScore = collected
                        maxRow = row
                        maxCol = col
                    }
                }
            }

            val maxCenter = boardMaxIdx - windowHalf
            val focus = Pos(
                (maxRow + windowHalf).coerceIn(windowHalf, maxCenter),
                (maxCol + windowHalf).coerceIn(windowHalf, maxCenter),
            )

            FocusInfo(focus, highlights)
        } ?: FocusInfo(Pos.CENTER, listOf(Pos.CENTER))

    fun resolveCenter(state: GameState, range: IntRange): FocusInfo {
        val lastPos = state.history.lastOrNull()

        return if (lastPos == null) {
            FocusInfo(Pos.CENTER, listOf(Pos.CENTER))
        } else {
            FocusInfo(
                Pos(lastPos.row.coerceIn(range), lastPos.col.coerceIn(range)),
                emptyList(),
            )
        }
    }

    object FocusWeights {

        const val LAST_MOVE: Int = 1000
        const val CENTER_EXTRA: Int = 1

        const val POTENTIAL: Int = 2

        const val CLOSED_FOUR: Int = 2
        const val OPEN_THREE: Int = 3

        const val BLOCK_THREE: Int = 10
        const val OPEN_FOUR: Int = 1000
        const val FIVE: Int = 1000

        const val BLOCK_FOUR_EXTRA: Int = 8
        const val TREAT_BLOCK_THREE_FORK: Int = BLOCK_THREE

        const val DOUBLE_THREE_FORK: Int = 30
        const val THREE_FOUR_FORK: Int = 50
        const val DOUBLE_FOUR_FORK: Int = 50

    }

}
