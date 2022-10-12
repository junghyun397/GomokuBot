package core.inference

import core.assets.Notation
import core.assets.intToStruct
import engine.move.LargeMoveGenerator
import engine.search.vcf.VCFSolver
import renju.*
import renju.notation.Pos
import renju.notation.Renju
import renju.protocol.Solution
import renju.protocol.SolutionLeaf
import utils.assets.bound
import utils.assets.maxIndexes
import utils.assets.toInt
import utils.lang.pair
import java.util.*
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    object FocusWeights : WeightSet {

        const val lastMove = 1000
        const val centerExtra = 1

        override val neighborhoodExtra = 2

        override val closedFour = 2
        override val openThree = 3

        override val blockThree = 10
        override val openFour = 150
        override val five = 400

        override val blockFourExtra = 0
        override val treatBlockThreeFork = this.blockThree

        override val threeSideTrap = 0
        override val fourSideTrap = 0
        override val treatThreeSideTrapFork = 0

        override val doubleThreeFork = 30
        override val threeFourFork = 50
        override val doubleFourFork = 50

        const val fiveComponent = 400
        const val fiveGuide = 200

    }

    object SolverWeights : WeightSet {

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

    private fun isStoneExist(board: Board, row: Int, col: Int) =
        row in 0 until Renju.BOARD_WIDTH() && col in 0 until Renju.BOARD_WIDTH()
                && Notation.FlagInstance.isExist(board.field()[Pos.rowColToIdx(row, col)])

    private fun hasNeighborhood(board: Board, idx: Int): Boolean {
        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        return (isStoneExist(board, row + 1, col - 1) || isStoneExist(board, row + 1, col) || isStoneExist(board, row + 1, col + 1) ||
                isStoneExist(board, row, col - 1) || isStoneExist(board, row, col + 1) ||
                isStoneExist(board, row - 1, col - 1) || isStoneExist(board, row - 1, col) || isStoneExist(board, row - 1, col + 1))
    }

    private fun collectFiveComponentsInStrip(board: Board, strip: L1Strip, color: Byte): Pair<List<Int>, List<Int>> {
        val components = mutableListOf<Int>()

        for (idx in 0 until strip.size()) {
            val absoluteIdx = when (strip.direction()) {
                Notation.Direction.X -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()), Pos.idxToCol(strip.startIdx()) + idx)
                Notation.Direction.Y -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()))
                Notation.Direction.IncreaseUp -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()) + idx)
                Notation.Direction.DescentUp -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()) - idx)
                else -> throw IllegalStateException()
            }

            val flag = strip.stripField()[idx]

            if (intToStruct(board.getFieldStatus(absoluteIdx).apply(color)).fiveAt(strip.direction()) || flag == color)
                components.add(absoluteIdx)
            else
                components.clear()

            if (components.size == 5) {
                return components pair when (strip.direction()) {
                    Notation.Direction.X -> mutableListOf<Int>().apply {
                        val centerRow = Pos.idxToRow(components[2])
                        val centerCol = Pos.idxToCol(components[2])

                        if (centerRow < Renju.BOARD_WIDTH() - 2) add(Pos.rowColToIdx(centerRow + 2, centerCol))
                        if (centerRow < Renju.BOARD_WIDTH() - 1) add(Pos.rowColToIdx(centerRow + 1, centerCol))
                        if (centerRow > 0) add(Pos.rowColToIdx(centerRow - 1, centerCol))
                        if (centerRow > 1) add(Pos.rowColToIdx(centerRow - 2, centerCol))
                    }

                    Notation.Direction.Y -> mutableListOf<Int>().apply {
                        val centerRow = Pos.idxToRow(components[2])
                        val centerCol = Pos.idxToCol(components[2])

                        if (centerCol < Renju.BOARD_WIDTH() - 2) add(Pos.rowColToIdx(centerRow, centerCol + 2))
                        if (centerCol < Renju.BOARD_WIDTH() - 1) add(Pos.rowColToIdx(centerRow, centerCol + 1))
                        if (centerCol > 0) add(Pos.rowColToIdx(centerRow, centerCol - 1))
                        if (centerCol > 1) add(Pos.rowColToIdx(centerRow, centerCol - 2))
                    }

                    else -> emptyList()
                }
            }
        }

        return emptyList<Int>() pair emptyList()
    }

    private fun collectFiveComponents(board: Board): Pair<List<Int>, List<Int>> {
        val components = BoardOps(board).composeStrips(board.lastMove())
            .map { strip -> this.collectFiveComponentsInStrip(board, strip, board.colorFlag()) }

        return components.flatMap { (components, _) -> components } pair components.flatMap { (_, guides) -> guides }
    }

    private fun evaluateParticle(weightSet: WeightSet, selfStruct: Struct, opponentStruct: Struct, bySelf: Boolean): Int {
        val forkWeight = when {
            selfStruct.fourTotal() > 1 -> weightSet.doubleFourFork
            selfStruct.threeTotal() > 0 && selfStruct.fourTotal() > 0 -> weightSet.threeFourFork
            selfStruct.threeTotal() > 1 -> weightSet.doubleThreeFork
            else -> 0
        }

        val treatWeight = selfStruct.threeTotal() * weightSet.openThree + selfStruct.closedFourTotal() * weightSet.closedFour

        val fourWeight = when {
            bySelf -> selfStruct.openFourTotal() * weightSet.openFour
            else -> when {
                selfStruct.blockThreeTotal() > 0 -> when {
                    opponentStruct.threeTotal() > 0 || opponentStruct.fourTotal() > 0 -> weightSet.treatBlockThreeFork
                    else -> weightSet.blockThree + selfStruct.openFourTotal() * weightSet.blockFourExtra
                }
                else -> 0
            }
        }

        val fiveWeight = selfStruct.fiveTotal() * weightSet.five

        return forkWeight + treatWeight + fourWeight + fiveWeight
    }

    fun findSolution(board: Board, weightSet: WeightSet = SolverWeights): Solution {
        val traps = StructOps(board).collectTrapPoints()

        val moves = LargeMoveGenerator.collectValidMoves(board)

        if (moves.size == 1)
            return SolutionLeaf(moves.first())

        val eval = moves.map { idx ->
            val fieldStatus = board.getFieldStatus(idx)
            val selfStruct = intToStruct(fieldStatus.apply(board.nextColorFlag()))
            val opponentStruct = intToStruct(fieldStatus.apply(board.colorFlag()))

            this.evaluateParticle(weightSet, selfStruct, opponentStruct, true) +
                    this.evaluateParticle(weightSet, opponentStruct, selfStruct, false) +
                    this.hasNeighborhood(board, idx).toInt() * weightSet.neighborhoodExtra +
                    when {
                        traps._1.contains(idx) -> when {
                            selfStruct.threeTotal() > 0 || selfStruct.fourTotal() > 0 -> weightSet.treatThreeSideTrapFork
                            else -> weightSet.threeSideTrap
                        }
                        else -> 0
                    } +
                    traps._2.contains(idx).toInt() * weightSet.fourSideTrap
        }

        val maxIndexes = eval.maxIndexes()!!

        if (moves.size > 2) {
            val vcfSequence = VCFSolver.findVCFSequence(board)

            if (vcfSequence.nonEmpty())
                return Solution.fromIterable(vcfSequence).get()
        }

        return SolutionLeaf(moves[maxIndexes.random()])
    }

    private fun evaluateBoard(board: Board): MutableList<MutableList<Int>> {
        val (fiveComponents, fiveGuides) = this.collectFiveComponents(board)

        return (0 until Renju.BOARD_SIZE())
            .map { idx ->
                val flag = board.field()[idx]

                if (Notation.FlagInstance.isForbid(flag, board.nextColorFlag()))
                    0
                else {
                    val fieldStatus = board.getFieldStatus(idx)
                    val selfStruct = intToStruct(fieldStatus.apply(board.nextColorFlag()))
                    val opponentStruct = intToStruct(fieldStatus.apply(board.colorFlag()))

                    this.evaluateParticle(FocusWeights, selfStruct, opponentStruct, true) +
                            this.evaluateParticle(FocusWeights, opponentStruct, selfStruct, true) +
                            this.hasNeighborhood(board, idx).toInt() * FocusWeights.neighborhoodExtra +
                            fiveComponents.contains(idx).toInt() * FocusWeights.fiveComponent +
                            fiveGuides.contains(idx).toInt() * FocusWeights.fiveGuide
                }
            }
            .chunked(Renju.BOARD_WIDTH()) { it.toMutableList() }
            .toMutableList()
    }

    data class FocusInfo(val focus: Pos, val highlights: List<Pos>)

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: Board, kernelWidth: Int, buildHighlights: Boolean): FocusInfo =
        board.lastPos().fold(
            { FocusInfo(Renju.BOARD_CENTER_POS(), listOf(Renju.BOARD_CENTER_POS())) },
            { lastPos ->
                val kernelHalf = kernelWidth / 2
                val kernelQuarter = kernelWidth / 4

                val evaluated = this.evaluateBoard(board)

                val highlights = when (buildHighlights) {
                    true -> evaluated
                        .asSequence()
                        .flatten()
                        .withIndex()
                        .filter { (_, value) -> value >= FocusWeights.five }
                        .map { (index, _) -> Pos.fromIdx(index) }
                        .toList()
                    else -> emptyList()
                }

                evaluated[lastPos.row()][lastPos.col()] += FocusWeights.lastMove

                for (row in (lastPos.row() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), lastPos.row() + kernelHalf))
                    for (col in (lastPos.col() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), lastPos.col() + kernelHalf))
                        evaluated[row][col] += FocusWeights.centerExtra

                if (board.moves() < 5) {
                    for (row in (lastPos.row() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), lastPos.row() + kernelQuarter))
                        for (col in (lastPos.col() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), lastPos.col() + kernelQuarter))
                            evaluated[row][col] += FocusWeights.centerExtra
                }

                val prefix = evaluated
                    .map { row -> row.runningFold(0) { acc, weight -> acc + weight } }
                    .runningFold(Collections.nCopies(Renju.BOARD_WIDTH() + 1, 0)) { acc, col ->
                        acc.zip(col).map { it.first + it.second }
                    }
                    .toMutableList()

                val step = Renju.BOARD_WIDTH() - kernelWidth

                var maxScore = 0
                var maxRow = lastPos.row()
                var maxCol = lastPos.col()

                for (row in (0 .. step))
                    for (col in (0 .. step)) {
                        val collected = prefix[row + kernelWidth][col + kernelWidth] - prefix[row][col + kernelWidth] -
                                prefix[row + kernelWidth][col] + prefix[row][col]

                        if (collected > maxScore) {
                            maxScore = collected
                            maxRow = row
                            maxCol = col
                        }
                    }

                FocusInfo(Pos(max(kernelHalf, maxRow + kernelHalf), max(kernelHalf, maxCol + kernelHalf)), highlights)
            }
        )

    fun resolveCenter(board: Board, range: IntRange): FocusInfo =
        board.lastPos().fold(
            { FocusInfo(Renju.BOARD_CENTER_POS(), listOf(Renju.BOARD_CENTER_POS())) },
            { FocusInfo(Pos(it.row().coerceIn(range), it.col().coerceIn(range)), listOf(Renju.BOARD_CENTER_POS())) }
        )

}
