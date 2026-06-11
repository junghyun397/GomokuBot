package core.session.entities

import renju.notation.Pos

sealed interface TaraguchiOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.TARAGUCHI_10

}

data class TaraguchiSwapStageSession(
    override val context: GameSessionContext,
    override val isBranched: Boolean
) : TaraguchiOpeningSession, SwapStageOpeningSession {

    override val offerCount = null

    override fun swap(doSwap: Boolean): GameSession {
        val user = if (doSwap) this.user.swap() else this.user

        return if (this.isBranched && this.state.history.moves == 5)
            PvpGameSession(
                context = this.context.next(user = user),
                gameResult = this.gameResult,
                recording = true,
                ruleKind = Rule.TARAGUCHI_10,
            )
        else
            TaraguchiMoveStageSession(
                context = this.context.next(user = user),
                isBranched = this.isBranched
            )
    }

}

data class TaraguchiMoveStageSession(
    override val context: GameSessionContext,
    override val isBranched: Boolean
) : TaraguchiOpeningSession, MoveStageOpeningSession {

    override fun validateMove(move: Pos) = this.inSquare(move)

    override fun inSquare(move: Pos): Boolean =
        6 - this.state.history.moves < move.row && move.row < 8 + this.state.history.moves &&
                6 - this.state.history.moves < move.col && move.col < 8 + this.state.history.moves

    override fun next(move: Pos): TaraguchiOpeningSession =
        if (this.state.history.moves == 3)
            TaraguchiBranchingSession(
                context = this.context.next(state = this.state.play(move))
            )
        else
            TaraguchiSwapStageSession(
                context = this.context.next(state = this.state.play(move)),
                isBranched = this.isBranched
            )

}

data class TaraguchiBranchingSession(
    override val context: GameSessionContext
) : TaraguchiOpeningSession, BranchingStageOpeningSession {

    override fun branch(makeOffer: Boolean): TaraguchiOpeningSession =
        if (makeOffer)
            TaraguchiOfferStageSession(
                context = this.context.next(),
                moveCandidates = emptySet(),
                symmetryMoves = emptySet()
            )
        else
            TaraguchiSwapStageSession(
                context = this.context.next(),
                isBranched = true
            )

}

data class TaraguchiOfferStageSession(
    override val context: GameSessionContext,
    override val moveCandidates: Set<Pos>,
    override val symmetryMoves: Set<Pos>
) : TaraguchiOpeningSession, OfferStageOpeningSession {

    override val remainingMoves get() = 10 - this.moveCandidates.size

    override fun add(move: Pos): NegotiateStageOpeningSession =
        if (this.moveCandidates.size < 9)
            this.copy(
                context = this.context.copy(messageBufferKey = MessageBufferKey.issue()),
                moveCandidates = this.moveCandidates + move,
                symmetryMoves = this.symmetryMoves + this.calculateSymmetryMoves(move),
            )
        else
            TaraguchiSelectStageSession(
                context = this.context.next(),
                moveCandidates = this.moveCandidates + move,
            )

}

data class TaraguchiSelectStageSession(
    override val context: GameSessionContext,
    override val moveCandidates: Set<Pos>,
) : TaraguchiOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            context = this.context.next(state = this.state.play(move)),
            gameResult = this.gameResult,
            recording = true,
            ruleKind = Rule.TARAGUCHI_10,
        )

}
