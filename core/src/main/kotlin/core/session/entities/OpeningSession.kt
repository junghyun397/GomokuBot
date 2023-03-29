package core.session.entities

import renju.notation.Pos
import utils.structs.Option

sealed interface OpeningSession : GameSession {

    override val gameResult get() = Option.Empty

    override val recording get() = false

    fun validateMove(move: Pos): Boolean = true

    override fun updateResult(gameResult: GameResult) = this.asFinishedPvpSession(gameResult)

}

sealed interface PlayStageOpeningSession : OpeningSession {

    val isBranched: Boolean

}

interface SwapStageOpeningSession : PlayStageOpeningSession {

    fun swap(doSwap: Boolean): GameSession

}

interface MoveStageOpeningSession : PlayStageOpeningSession {

    fun inSquare(move: Pos): Boolean

    fun next(move: Pos) : OpeningSession

}

interface PatternStageOpeningSession : MoveStageOpeningSession {

    override val player get() =
        if (this.ownerHasBlack) this.owner
        else this.opponent

    override val nextPlayer get() =
        if ((this.board.moves() == 2) == this.ownerHasBlack) this.opponent
        else this.owner

}

interface BranchingStageOpeningSession : OpeningSession {

    fun branch(makeOffer: Boolean) : OpeningSession

}

sealed interface NegotiateStageOpeningSession : OpeningSession {

    val moveCandidates: Set<Pos>

    val remainingMoves: Int

}

interface DeclareStageOpeningSession : NegotiateStageOpeningSession {

    fun declare(count: Int): OfferStageOpeningSession

}

interface OfferStageOpeningSession : NegotiateStageOpeningSession {

    override fun validateMove(move: Pos) = move !in this.moveCandidates

    fun add(move: Pos): NegotiateStageOpeningSession

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
        gameResult = Option.Some(result),
        history = history,
        messageBufferKey = messageBufferKey,
        recording = false,
        ruleKind = ruleKind,
        expireService = expireService,
    )
