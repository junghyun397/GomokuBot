package core.inference

import renju.Board
import renju.native.RustyRenjuCApi
import renju.notation.Color
import renju.notation.Pos
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    data class FocusInfo(val focus: Pos, val highlights: List<Pos>)

    private data class PatternStats(
        val closedFour: Int,
        val openFour: Int,
        val openThree: Int,
        val closeThree: Int,
        val five: Int,
    ) {
        val fourTotal: Int
            get() = closedFour + openFour

        val threeTotal: Int
            get() = openThree + closeThree
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private fun isStoneExist(field: ByteArray, row: Int, col: Int): Boolean =
        row in 0 until Pos.BOARD_WIDTH &&
                col in 0 until Pos.BOARD_WIDTH &&
                Color.isStone(field[Pos.rowColToIdx(row, col)])

    private fun hasNeighborhood(field: ByteArray, idx: Int): Boolean {
        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        return isStoneExist(field, row + 1, col - 1) ||
                isStoneExist(field, row + 1, col) ||
                isStoneExist(field, row + 1, col + 1) ||
                isStoneExist(field, row, col - 1) ||
                isStoneExist(field, row, col + 1) ||
                isStoneExist(field, row - 1, col - 1) ||
                isStoneExist(field, row - 1, col) ||
                isStoneExist(field, row - 1, col + 1)
    }

    private fun buildPatternStats(pattern: Int): PatternStats {
        val masks = RustyRenjuCApi.constants

        return PatternStats(
            closedFour = (pattern and masks.closedFourMask).countOneBits(),
            openFour = (pattern and masks.openFourMask).countOneBits(),
            openThree = (pattern and masks.openThreeMask).countOneBits(),
            closeThree = (pattern and masks.closeThreeMask).countOneBits(),
            five = (pattern and masks.fiveMask).countOneBits(),
        )
    }

    private fun evaluateParticle(weightSet: WeightSet, self: PatternStats, opponent: PatternStats, bySelf: Boolean): Int {
        val forkWeight = when {
            self.fourTotal > 1 -> weightSet.doubleFourFork
            self.threeTotal > 0 && self.fourTotal > 0 -> weightSet.threeFourFork
            self.threeTotal > 1 -> weightSet.doubleThreeFork
            else -> 0
        }

        val treatWeight = self.threeTotal * weightSet.openThree +
                self.closedFour * weightSet.closedFour

        val fourWeight = when {
            bySelf -> self.openFour * weightSet.openFour
            self.closeThree > 0 -> when {
                opponent.threeTotal > 0 || opponent.fourTotal > 0 -> weightSet.treatBlockThreeFork
                else -> weightSet.blockThree + self.openFour * weightSet.blockFourExtra
            }
            else -> 0
        }

        val fiveWeight = self.five * weightSet.five

        return forkWeight + treatWeight + fourWeight + fiveWeight
    }

    private fun isLegalEmptyMove(board: Board, field: ByteArray, idx: Int): Boolean =
        !Color.isStone(field[idx]) && board.validateMove(idx).isNone()

    private fun collectFiveHighlights(board: Board, field: ByteArray): List<Pos> {
        val fiveMask = RustyRenjuCApi.constants.fiveMask
        val nextColor = board.playerColor()

        return (0 until Pos.BOARD_SIZE)
            .asSequence()
            .filter { idx -> isLegalEmptyMove(board, field, idx) }
            .filter { idx -> (board.pattern(idx, nextColor) and fiveMask) != 0 }
            .map { idx -> Pos.fromIdx(idx) }
            .toList()
    }

    private fun evaluateBoard(board: Board, field: ByteArray): MutableList<MutableList<Int>> {
        val nextColor = board.playerColor()
        val opponentColor = nextColor.reversed()

        return (0 until Pos.BOARD_SIZE)
            .map { idx ->
                if (!isLegalEmptyMove(board, field, idx)) {
                    0
                } else {
                    val self = buildPatternStats(board.pattern(idx, nextColor))
                    val opponent = buildPatternStats(board.pattern(idx, opponentColor))

                    evaluateParticle(FocusWeights, self, opponent, true) +
                            evaluateParticle(FocusWeights, opponent, self, false) +
                            hasNeighborhood(field, idx).toInt() * FocusWeights.neighborhoodExtra
                }
            }
            .chunked(Pos.BOARD_WIDTH) { row -> row.toMutableList() }
            .toMutableList()
    }

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: Board, kernelWidth: Int, buildHighlights: Boolean): FocusInfo =
        board.lastPos().fold(
            ifEmpty = { FocusInfo(Pos.CENTER, listOf(Pos.CENTER)) },
            ifSome = { lastPos ->
                val boardWidth = Pos.BOARD_WIDTH
                val boardMaxIdx = Pos.BOARD_BOUND
                val normalizedKernelWidth = kernelWidth.coerceIn(1, boardWidth)
                val kernelHalf = normalizedKernelWidth / 2
                val kernelQuarter = normalizedKernelWidth / 4

                val field = board.field
                val evaluated = evaluateBoard(board, field)
                val highlights = if (buildHighlights) collectFiveHighlights(board, field) else emptyList()

                evaluated[lastPos.row()][lastPos.col()] += FocusWeights.lastMove

                for (row in max(0, lastPos.row() - kernelHalf)..min(boardMaxIdx, lastPos.row() + kernelHalf)) {
                    for (col in max(0, lastPos.col() - kernelHalf)..min(boardMaxIdx, lastPos.col() + kernelHalf)) {
                        evaluated[row][col] += FocusWeights.centerExtra
                    }
                }

                if (board.moves() < 5) {
                    for (row in max(0, lastPos.row() - kernelQuarter)..min(boardMaxIdx, lastPos.row() + kernelQuarter)) {
                        for (col in max(0, lastPos.col() - kernelQuarter)..min(boardMaxIdx, lastPos.col() + kernelQuarter)) {
                            evaluated[row][col] += FocusWeights.centerExtra
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

                val step = boardWidth - normalizedKernelWidth

                var maxScore = Int.MIN_VALUE
                var maxRow = lastPos.row().coerceIn(0, step)
                var maxCol = lastPos.col().coerceIn(0, step)

                for (row in 0..step) {
                    for (col in 0..step) {
                        val collected = prefix[row + normalizedKernelWidth][col + normalizedKernelWidth] -
                                prefix[row][col + normalizedKernelWidth] -
                                prefix[row + normalizedKernelWidth][col] +
                                prefix[row][col]

                        if (collected > maxScore) {
                            maxScore = collected
                            maxRow = row
                            maxCol = col
                        }
                    }
                }

                val minCenter = kernelHalf
                val maxCenter = boardMaxIdx - kernelHalf
                val focus = Pos(
                    (maxRow + kernelHalf).coerceIn(minCenter, maxCenter),
                    (maxCol + kernelHalf).coerceIn(minCenter, maxCenter),
                )

                FocusInfo(focus, highlights)
            }
        )

    fun resolveCenter(board: Board, range: IntRange): FocusInfo {
        val lastPos = board.lastPos().getOrNull()

        return if (lastPos == null) {
            FocusInfo(Pos.CENTER, listOf(Pos.CENTER))
        } else {
            FocusInfo(
                Pos(lastPos.row().coerceIn(range), lastPos.col().coerceIn(range)),
                emptyList(),
            )
        }
    }

    object FocusWeights : WeightSet {

        const val lastMove: Int = 1000
        const val centerExtra: Int = 1

        override val neighborhoodExtra: Int = 2

        override val closedFour: Int = 2
        override val openThree: Int = 3

        override val blockThree: Int = 10
        override val openFour: Int = 150
        override val five: Int = 400

        override val blockFourExtra: Int = 0
        override val treatBlockThreeFork: Int = blockThree

        override val threeSideTrap: Int = 0
        override val fourSideTrap: Int = 0
        override val treatThreeSideTrapFork: Int = 0

        override val doubleThreeFork: Int = 30
        override val threeFourFork: Int = 50
        override val doubleFourFork: Int = 50

    }

}
