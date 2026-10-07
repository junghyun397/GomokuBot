package core.engine

import renju.Board
import renju.GameState
import renju.native.RustyRenju
import renju.notation.Color
import renju.notation.Pos
import renju.notation.asList
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    data class BoardFocus(val focus: Pos, val hints: List<Pos>?)

    private fun evaluateBoard(state: GameState): List<Int> {
        val opponentColor = !state.board.playerColor

        fun Int.count(mask: Int): Int = (this and mask).countOneBits()
        fun Int.openFours(): Int = count(RustyRenju.openFourMask)
        fun Int.closedFours(): Int = count(RustyRenju.closedFourMask)
        fun Int.threes(): Int = count(RustyRenju.openThreeMask)

        fun Int.forkScore(weights: FocusWeights): Int = when {
            closedFours() > 1 -> weights.forkFour                       // double-four
            threes() > 0 && closedFours() > 0 -> weights.threeFourFork  // three-four
            threes() > 1 -> weights.doubleThreeFork                     // double-three
            else -> 0
        }

        fun Int.score(weights: FocusWeights): Int = forkScore(weights) +
                threes() * weights.openThree +
                openFours() * weights.forkFour +
                closedFours() * weights.closedFour +
                count(RustyRenju.closeThreeMask) * weights.closeThree +
                count(RustyRenju.potentialFourMask) * weights.potentialFour +
                count(RustyRenju.potentialThreeMask) * weights.potentialThree

        return (0 until Pos.BOARD_SIZE)
            .map(Pos::fromIdx)
            .map { pos ->
                when (state.board.stoneKind(pos)) {
                    null -> {
                        state.board.pattern(pos, state.board.playerColor).score(PlayerFocusWeights) +
                                state.board.pattern(pos, opponentColor).score(OpponentFocusWeights)
                    }
                    state.board.playerColor -> PlayerFocusWeights.stone
                    else -> OpponentFocusWeights.stone
                }
            }
    }

    private fun findFiveComponents(board: Board, fivePos: Pos): List<Pos> {
        val color = Color.entries.firstOrNull { fivePos in board.fivePos[it] } ?: return emptyList()

        for ((dr, dc) in listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)) {
            var stones = 1 shl 4
            for (step in -4 .. 4) {
                val row = fivePos.row + dr * step
                val col = fivePos.col + dc * step

                if (row in 0 .. Pos.BOARD_BOUND && col in 0 .. Pos.BOARD_BOUND && board.stoneKind(Pos(row, col)) == color) {
                    stones = stones or (1 shl (step + 4))
                }
            }

            val five = stones and
                    (stones ushr 1) and
                    (stones ushr 2) and
                    (stones ushr 3) and
                    (stones ushr 4)

            if (five == 0) continue

            val start = five.countTrailingZeroBits() - 4
            return (start..start + 4).map { step -> Pos(fivePos.row + dr * step, fivePos.col + dc * step) }
        }

        return emptyList()
    }

    // Prefix Sum, O(N)
    fun resolveFocus(state: GameState, windowWidth: Int, hints: Boolean): BoardFocus {
        val lastPos = state.history.lastOrNull() ?: return BoardFocus(Pos.CENTER, listOf(Pos.CENTER))

        val clampedWindowWidth = windowWidth.coerceIn(1, Pos.BOARD_WIDTH)
        val windowHalf = clampedWindowWidth / 2

        val scores = this.evaluateBoard(state).toMutableList()

        scores[lastPos.idx] = PlayerFocusWeights.lastMove

        state.history.getOrNull(state.history.lastIndex - 1)?.let { opponentPos ->
            scores[opponentPos.idx] += OpponentFocusWeights.lastMove
        }

        if (hints) {
            state.board.fivePos.asList().flatten()
                .filterNotNull()
                .firstOrNull()
                ?.let { this.findFiveComponents(state.board, it) }
                ?.forEach { scores[it.idx] += SharedFocusWeight.FIVE_COMPONENTS }
        }

        val hintPos = scores.mapIndexedNotNull { index, score -> if (score > 1000) Pos.fromIdx(index) else null }

        fun applyCentering(halfWindowSize: Int) {
            for (row in max(0, lastPos.row - halfWindowSize) .. min(Pos.BOARD_BOUND, lastPos.row + halfWindowSize)) {
                for (col in max(0, lastPos.col - halfWindowSize) .. min(Pos.BOARD_BOUND, lastPos.col + halfWindowSize)) {
                    scores[Pos.rowColToIdx(row, col)] += SharedFocusWeight.CENTERING
                }
            }
        }

        applyCentering(windowHalf)
        applyCentering(clampedWindowWidth / 4)

        val chunkedScores = scores
            .chunked(Pos.BOARD_WIDTH)

        val prefix = Array(Pos.BOARD_WIDTH + 1 ) { IntArray(Pos.BOARD_WIDTH + 1) }

        for (row in 1 .. Pos.BOARD_WIDTH) {
            for (col in 1 .. Pos.BOARD_WIDTH) {
                prefix[row][col] = chunkedScores[row - 1][col - 1] +
                        prefix[row - 1][col] +
                        prefix[row][col - 1] -
                        prefix[row - 1][col - 1]
            }
        }

        val step = Pos.BOARD_WIDTH - clampedWindowWidth

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

        val maxCenter = Pos.BOARD_BOUND - windowHalf
        val focus = Pos(
            (maxRow + windowHalf).coerceIn(windowHalf, maxCenter),
            (maxCol + windowHalf).coerceIn(windowHalf, maxCenter),
        )

        return BoardFocus(focus, hintPos)
    }

    fun resolveCenter(state: GameState, range: IntRange): BoardFocus {
        val lastPos = state.history.lastOrNull()

        return if (lastPos == null) {
            BoardFocus(Pos.CENTER, listOf(Pos.CENTER))
        } else {
            BoardFocus(
                Pos(lastPos.row.coerceIn(range), lastPos.col.coerceIn(range)),
                emptyList(),
            )
        }
    }

    object SharedFocusWeight {
        const val CENTERING: Int = 1
        const val FIVE_COMPONENTS: Int = 5000
    }

    interface FocusWeights {
        val lastMove: Int

        val stone: Int

        val closedFour: Int
        val openThree: Int
        val closeThree: Int
        val potentialThree: Int
        val potentialFour: Int

        val doubleThreeFork: Int
        val threeFourFork: Int
        val forkFour: Int
    }

    object PlayerFocusWeights : FocusWeights {
        override val lastMove: Int = 1000

        override val stone: Int = 1

        override val closedFour: Int = 2
        override val openThree: Int = 10
        override val closeThree: Int = 0
        override val potentialThree: Int = 5
        override val potentialFour: Int = 1

        override val doubleThreeFork: Int = 200
        override val threeFourFork: Int = 500
        override val forkFour: Int = 1500
    }

    object OpponentFocusWeights : FocusWeights {
        override val lastMove: Int = 300

        override val stone: Int = 0

        override val closedFour: Int = 1
        override val openThree: Int = 7
        override val closeThree: Int = 300
        override val potentialThree: Int = 2
        override val potentialFour: Int = 1

        override val doubleThreeFork: Int = 100
        override val threeFourFork: Int = 300
        override val forkFour: Int = 1000
    }

}
