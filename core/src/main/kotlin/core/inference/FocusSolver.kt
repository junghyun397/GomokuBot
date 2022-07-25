package core.inference

import jrenju.*
import jrenju.notation.Direction
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import jrenju.protocol.Solution
import jrenju.protocol.SolutionLeaf
import jrenju.solve.LRUMemo
import jrenju.solve.`LargeMoveGenerator$`
import jrenju.solve.`SolutionMapper$`
import jrenju.solve.`VCFSolver$`
import utils.assets.bound
import utils.assets.maxSet
import utils.assets.toInt
import java.util.*
import kotlin.math.max
import kotlin.math.min

object FocusSolver {

    object FocusWeights : WeightSet() {

        const val latestMove = 500
        const val centerExtra = 1

        override val neighborhoodExtra = 2

        override val closedFour = 2
        override val openThree = 3

        override val blockThree = 10
        override val openFour = 150
        override val five = 200

        override val blockFourExtra = 0
        override val treatBlockThreeFork = this.blockThree

        override val threeSideTrap = 0
        override val fourSideTrap = 0
        override val treatThreeSideTrapFork = 0

        override val doubleThreeFork = 30
        override val threeFourFork = 50
        override val doubleFourFork = 50

        const val fiveComponent = 200

    }

    object SolverWeights : WeightSet() {

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
                && Flag.isExist(board.boardField()[Pos.rowColToIdx(row, col)])

    private fun hasNeighborhood(board: Board, idx: Int): Boolean {
        val row = Pos.idxToRow(idx)
        val col = Pos.idxToCol(idx)

        return (isStoneExist(board, row + 1, col - 1) || isStoneExist(board, row + 1, col) || isStoneExist(board, row + 1, col + 1) ||
                isStoneExist(board, row, col - 1) || isStoneExist(board, row, col + 1) ||
                isStoneExist(board, row - 1, col - 1) || isStoneExist(board, row - 1, col) || isStoneExist(board, row - 1, col + 1))
    }

    private fun collectFiveComponentsInStrip(board: Board, strip: L1Strip, color: Byte): List<Int> {
        val components = mutableListOf<Int>()
        for (idx in 0 until strip.size()) {
            val absoluteIdx = when (strip.direction()) {
                Direction.X() -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()), Pos.idxToCol(strip.startIdx()) + idx)
                Direction.Y() -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()))
                Direction.DEG45() -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()) + idx)
                Direction.DEG315() -> Pos.rowColToIdx(Pos.idxToRow(strip.startIdx()) + idx, Pos.idxToCol(strip.startIdx()) - idx)
                else -> throw IllegalStateException()
            }

            val flag = strip.stripField()[idx]

            if (board.getParticlePair(absoluteIdx).apply(color).fiveAt(strip.direction()) || flag == color)
                components.add(absoluteIdx)
            else
                components.clear()

            if (components.size == 5) {
                return when (strip.direction()) {
                    Direction.X() -> components.apply {
                        val centerRow = Pos.idxToRow(components[2])
                        val centerCol = Pos.idxToCol(components[2])

                        if (centerRow < Renju.BOARD_WIDTH() - 2)
                            add(Pos.rowColToIdx(centerRow + 2, centerCol))
                        if (centerRow < Renju.BOARD_WIDTH() - 1)
                            add(Pos.rowColToIdx(centerRow + 1, centerCol))
                        if (centerRow > 0)
                            add(Pos.rowColToIdx(centerRow - 1, centerCol))
                        if (centerRow > 1)
                            add(Pos.rowColToIdx(centerRow - 2, centerCol))
                    }
                    Direction.Y() -> components.apply {
                        val centerRow = Pos.idxToRow(components[2])
                        val centerCol = Pos.idxToCol(components[2])

                        if (centerCol < Renju.BOARD_WIDTH() - 2)
                            add(Pos.rowColToIdx(centerRow, centerCol + 2))
                        if (centerCol < Renju.BOARD_WIDTH() - 1)
                            add(Pos.rowColToIdx(centerRow, centerCol + 1))
                        if (centerCol > 0)
                            add(Pos.rowColToIdx(centerRow, centerCol - 1))
                        if (centerCol > 1)
                            add(Pos.rowColToIdx(centerRow, centerCol - 2))
                    }
                    else -> components
                }
            }
        }

        return emptyList()
    }

    private fun collectFiveComponents(board: Board) =
        BoardOps(board).composeStrips(board.latestMove())
            .flatMap { strip -> this.collectFiveComponentsInStrip(board, strip, board.colorFlag()) }

    private fun evaluateParticle(weightSet: WeightSet, particle: ParticleOps, particleB: ParticleOps, bySelf: Boolean): Int {
        val forkWeight = when {
            particle.fourTotal() > 1 -> weightSet.doubleFourFork
            particle.threeTotal() > 0 && particle.fourTotal() > 0 -> weightSet.threeFourFork
            particle.threeTotal() > 1 -> weightSet.doubleThreeFork
            else -> 0
        }

        val treatWeight = particle.threeTotal() * weightSet.openThree + particle.closedFourTotal() * weightSet.closedFour

        val fourWeight = when {
            bySelf -> particle.openFourTotal() * weightSet.openFour
            else -> when {
                particle.blockThreeTotal() > 0 -> when {
                    particleB.threeTotal() > 0 || particleB.fourTotal() > 0 -> weightSet.treatBlockThreeFork
                    else -> weightSet.blockThree + particle.openFourTotal() * weightSet.blockFourExtra
                }
                else -> 0
            }
        }

        val fiveWeight = particle.fiveTotal() * weightSet.five

        return forkWeight + treatWeight + fourWeight + fiveWeight
    }

    fun findSolution(board: Board, weightSet: WeightSet = SolverWeights): Solution {
        val traps = StructOps(board).collectTrapPoints()

        val moves = `LargeMoveGenerator$`.`MODULE$`.collectValidMoves(board)

        if (moves.size == 1)
            return SolutionLeaf(moves.first())

        val eval = moves
            .map { idx ->
                val particlePair = board.getParticlePair(idx)
                val selfParticle = particlePair.apply(board.nextColorFlag())
                val opponentParticle = particlePair.apply(board.colorFlag())

                this.evaluateParticle(weightSet, selfParticle, opponentParticle, true) +
                        this.evaluateParticle(weightSet, opponentParticle, selfParticle, false) +
                        this.hasNeighborhood(board, idx).toInt() * weightSet.neighborhoodExtra +
                        run {
                            if (traps._1.contains(idx)) {
                                if (selfParticle.threeTotal() > 0 || selfParticle.fourTotal() > 0)
                                    weightSet.treatThreeSideTrapFork
                                else
                                    weightSet.threeSideTrap
                            } else 0
                        } +
                        traps._2.contains(idx).toInt() * weightSet.fourSideTrap
            }

        if (moves.isEmpty())
            println(`BoardIO$`.`MODULE$`.BoardToText(board).debugText())

        val maxSet = eval.maxSet()!!

        if (moves.size > 2) {
            val vcfSequence = `VCFSolver$`.`MODULE$`.VCFFinder(board).findVCFSequence(LRUMemo.empty(), Int.MAX_VALUE)

            if (!vcfSequence.isEmpty)
                return `SolutionMapper$`.`MODULE$`.SequenceToNode(vcfSequence).toSolution()
        }

        return SolutionLeaf(moves[maxSet.random()])
    }

    private fun evaluateBoard(board: Board): MutableList<MutableList<Int>> {
        val fiveComponents = this.collectFiveComponents(board)

        return (0 until Renju.BOARD_SIZE())
            .map { idx ->
                val flag = board.boardField()[idx]

                if (Flag.isForbid(flag, board.nextColorFlag()))
                    0
                else {
                    val particlePair = board.getParticlePair(idx)
                    val selfParticle = particlePair.apply(board.nextColorFlag())
                    val opponentParticle = particlePair.apply(board.colorFlag())

                    this.evaluateParticle(FocusWeights, selfParticle, opponentParticle, true) +
                            this.evaluateParticle(FocusWeights, opponentParticle, selfParticle, true) +
                            this.hasNeighborhood(board, idx).toInt() * FocusWeights.neighborhoodExtra +
                            fiveComponents.contains(idx).toInt() * FocusWeights.fiveComponent
                }
            }
            .chunked(Renju.BOARD_WIDTH()) { it.toMutableList() }
            .toMutableList()
    }

    // Prefix Sum Algorithm, O(N)
    fun resolveFocus(board: Board, kernelWidth: Int): Pos =
        board.latestPos().fold(
            { Renju.BOARD_CENTER_POS() },
            { latestPos ->
                val kernelHalf = kernelWidth / 2
                val kernelQuarter = kernelWidth / 4

                val evaluated = this.evaluateBoard(board)

                evaluated[latestPos.row()][latestPos.col()] += FocusWeights.latestMove

                for (row in (latestPos.row() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelHalf))
                    for (col in (latestPos.col() - kernelHalf).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelHalf))
                        evaluated[row][col] += FocusWeights.centerExtra

                if (board.moves() < 5) {
                    for (row in (latestPos.row() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.row() + kernelQuarter))
                        for (col in (latestPos.col() - kernelQuarter).bound() .. min(Renju.BOARD_WIDTH_MAX_IDX(), latestPos.col() + kernelQuarter))
                            evaluated[row][col] += FocusWeights.centerExtra
                }

                val prefix = evaluated
                    .map { it.runningFold(0) { acc, weight -> acc + weight } }
                    .runningFold(Collections.nCopies(Renju.BOARD_WIDTH() + 1, 0)) { acc, col ->
                        acc.zip(col).map { it.first + it.second }
                    }
                    .toMutableList()

                val step = Renju.BOARD_WIDTH() - kernelWidth

                var maxScore = 0
                var maxRow = latestPos.row()
                var maxCol = latestPos.col()

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

                Pos(max(kernelHalf, maxRow + kernelHalf), max(kernelHalf, maxCol + kernelHalf))
            }
        )

}
