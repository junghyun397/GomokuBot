package core.session.entities

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import renju.notation.GameResult
import renju.notation.Pos
import kotlin.math.pow

sealed interface OpeningSession : GameSession {

    override val gameResult get() = None

    override val recording get() = false

    fun validateMove(move: Pos): Boolean = true

    override fun updateResult(gameResult: GameResult) = this.asFinishedPvpSession(gameResult)

}

sealed interface PlayStageOpeningSession : OpeningSession {

    val isBranched: Boolean

}

interface SwapStageOpeningSession : PlayStageOpeningSession {

    val offerCount: Option<Int>

    fun swap(doSwap: Boolean): GameSession

}

interface MoveStageOpeningSession : PlayStageOpeningSession {

    fun inSquare(move: Pos): Boolean

    fun next(move: Pos) : OpeningSession

}

interface BranchingStageOpeningSession : OpeningSession {

    fun branch(makeOffer: Boolean) : OpeningSession

}

interface DeclareStageOpeningSession : OpeningSession {

    val maxOfferCount: Int

    fun declare(count: Int): OpeningSession

}

sealed interface NegotiateStageOpeningSession : OpeningSession {

    val moveCandidates: Set<Pos>

}

interface OfferStageOpeningSession : NegotiateStageOpeningSession {

    val remainingMoves: Int

    val symmetryMoves: Set<Pos>

    override fun validateMove(move: Pos) = move !in this.moveCandidates && move !in this.symmetryMoves

    fun add(move: Pos): NegotiateStageOpeningSession

    private fun calculateSymmetryMoves(ref1: Pos, ref2: Pos, move: Pos): Set<Pos> =
        if (ref1.row() == ref2.row() || ref1.col() == ref2.col()) {
            val reversedRow = ref1.row() + ref2.row() - move.row()
            val reversedCol = ref1.col() + ref2.col() - move.col()

            // . . | . .
            // . M | X .
            // __1_|_2__
            // . X | X .
            // . . | . .
            setOf(
                Pos(reversedRow, reversedCol),
                Pos(move.row(), reversedCol),
                Pos(reversedRow, move.col())
            )
        } else {
            // y=ax+b
            // a=(y1-y2)/(x1-x2)
            val slope = (ref1.row() - ref2.row()).toDouble() / (ref1.col() - ref2.col().toDouble())
            // b=y-ax
            val intercept = ref1.row() - slope * ref1.col()

            // 2(ax-y+b)/(a^2+1)
            val baseEval = 2 * (slope * move.col() - move.row() + intercept) / (slope.pow(2) + 1)

            // x'=x-2a(ax-y+b)/(a^2+1)
            val reversedCol = (move.col() - slope * baseEval).toInt()
            // y'=y+2(ax-y+b)/(a^2+1)
            val reversedRow = (move.row() + baseEval).toInt()

            // . M . . .
            // X 1 . . .
            // . . . 2 X
            // . . . X .
            setOf(
                Pos(reversedRow, reversedCol),
                Pos(ref1.row() + ref2.row() - reversedRow, ref1.col() + ref2.col() - reversedCol),
                Pos(ref1.row() + ref2.row() - move.row(), ref1.col() + ref2.col() - move.col())
            )
        }

    fun calculateSymmetryMoves(move: Pos): Set<Pos> {
        val blackSymmetryMoves = this.calculateSymmetryMoves(this.history[0]!!, this.history[2]!!, move)
        val whiteSymmetryMoves = this.calculateSymmetryMoves(this.history[1]!!, this.history[3]!!, move)

        return blackSymmetryMoves.intersect(whiteSymmetryMoves) - move
    }

}

interface SelectStageOpeningSession : NegotiateStageOpeningSession {

    override fun validateMove(move: Pos) = move in this.moveCandidates

    override val player get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.opponent
        else -> this.owner
    }

    override val nextPlayer get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.owner
        else -> this.opponent
    }

    fun select(move: Pos): PvpGameSession

}

fun OpeningSession.asFinishedPvpSession(result: GameResult): PvpGameSession =
    PvpGameSession(
        owner = owner,
        opponent = opponent,
        ownerHasBlack = ownerHasBlack,
        board = board,
        gameResult = Some(result),
        history = history,
        messageBufferKey = messageBufferKey,
        recording = false,
        ruleKind = ruleKind,
        expireService = expireService,
    )
