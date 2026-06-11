package core.session.entities

import renju.notation.Pos

sealed interface SoosyrvOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.SOOSYRV_8

}

data class SoosyrvMoveStageSession(
    override val context: GameSessionContext,
    override val isBranched: Boolean
) : SoosyrvOpeningSession, MoveStageOpeningSession {

    override fun validateMove(move: Pos) = this.inSquare(move)

    override fun inSquare(move: Pos): Boolean =
        this.isBranched ||
                6 - this.state.history.moves < move.row && move.row < 8 + this.state.history.moves &&
                6 - this.state.history.moves < move.col && move.col < 8 + this.state.history.moves

    override val player get() =
        if (this.state.history.moves < 2)
            this.user.black
        else super<MoveStageOpeningSession>.player

    override val opponent get() =
        if (this.state.history.moves < 2)
            this.user.white
        else super<MoveStageOpeningSession>.opponent

    override fun next(move: Pos): SoosyrvOpeningSession =
        if (this.isBranched)
            SoosyrvDeclareStageOpeningSession(
                context = this.context.next(state = this.state.play(move)),
            )
        else if (this.state.history.moves < 2)
            this.copy(
                context = this.context.next(state = this.state.play(move)),
            )
        else // moves == 3
            SoosyrvSwapStageSession(
                context = this.context.next(state = this.state.play(move)),
                offerCount = null
            )

}

data class SoosyrvSwapStageSession(
    override val context: GameSessionContext,
    override val offerCount: Int?
) : SoosyrvOpeningSession, SwapStageOpeningSession {

    override val isBranched = true

    override fun swap(doSwap: Boolean): SoosyrvOpeningSession {
        val user = if (doSwap) this.user.swap() else this.user

        return if (this.offerCount == null)
            SoosyrvMoveStageSession(
                context = this.context.next(user = user),
                isBranched = true
            )
        else
            SoosyrvOfferStageOpeningSession(
                context = this.context.next(user = user),
                moveCandidates = emptySet(),
                symmetryMoves = emptySet(),
                offerCount = this.offerCount
            )
    }

}

data class SoosyrvDeclareStageOpeningSession(
    override val context: GameSessionContext,
) : SoosyrvOpeningSession, DeclareStageOpeningSession {

    override val player get() = super<DeclareStageOpeningSession>.opponent

    override val opponent get() = super<DeclareStageOpeningSession>.player

    override val maxOfferCount = 8

    override fun declare(count: Int) =
        SoosyrvSwapStageSession(
            context = this.context.next(),
            offerCount = count
        )

}

data class SoosyrvOfferStageOpeningSession(
    override val context: GameSessionContext,
    override val moveCandidates: Set<Pos>,
    override val symmetryMoves: Set<Pos>,
    val offerCount: Int
) : SoosyrvOpeningSession, OfferStageOpeningSession {

    override val remainingMoves get() = this.offerCount - this.moveCandidates.size

    override fun add(move: Pos): NegotiateStageOpeningSession =
        if (this.remainingMoves == 1)
            SoosyrvSelectStageOpeningSession(
                context = this.context.next(),
                moveCandidates = this.moveCandidates + move
            )
        else
            this.copy(
                context = this.context.copy(messageBufferKey = MessageBufferKey.issue()),
                moveCandidates = this.moveCandidates + move,
                symmetryMoves = this.symmetryMoves + this.calculateSymmetryMoves(move),
            )

}

data class SoosyrvSelectStageOpeningSession(
    override val context: GameSessionContext,
    override val moveCandidates: Set<Pos>,
) : SoosyrvOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            context = this.context.next(state = this.state.play(move)),
            gameResult = this.gameResult,
            recording = true,
            ruleKind = Rule.SOOSYRV_8,
        )

}
