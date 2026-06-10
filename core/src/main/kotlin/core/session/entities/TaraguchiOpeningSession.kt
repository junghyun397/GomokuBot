package core.session.entities

import core.assets.User
import core.session.Rule
import renju.GameState
import renju.notation.Pos

sealed interface TaraguchiOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.TARAGUCHI_10

}

data class TaraguchiSwapStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val isBranched: Boolean
) : TaraguchiOpeningSession, SwapStageOpeningSession {

    override val offerCount = null

    override fun swap(doSwap: Boolean): GameSession =
        if (this.isBranched && this.history.moves == 5)
            PvpGameSession(
                id = this.id,
                blackPlayer = if (doSwap) this.whitePlayer else this.blackPlayer,
                whitePlayer = if (doSwap) this.blackPlayer else this.whitePlayer,
                state = this.state,
                gameResult = this.gameResult,
                messageBufferKey = MessageBufferKey.issue(),
                recording = true,
                ruleKind = Rule.TARAGUCHI_10,
                expireService = this.expireService.next()
            )
        else
            TaraguchiMoveStageSession(
                id = this.id,
                blackPlayer = if (doSwap) this.whitePlayer else this.blackPlayer,
                whitePlayer = if (doSwap) this.blackPlayer else this.whitePlayer,
                state = this.state,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next(),
                isBranched = this.isBranched
            )

}

data class TaraguchiMoveStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val isBranched: Boolean
) : TaraguchiOpeningSession, MoveStageOpeningSession {

    override fun validateMove(move: Pos) = this.inSquare(move)

    override fun inSquare(move: Pos): Boolean =
        6 - history.moves < move.row && move.row < 8 + history.moves &&
                6 - history.moves < move.col && move.col < 8 + history.moves

    override fun next(move: Pos): TaraguchiOpeningSession =
        if (this.history.moves == 3)
            TaraguchiBranchingSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state.play(move),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next()
            )
        else
            TaraguchiSwapStageSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state.play(move),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next(),
                isBranched = this.isBranched
            )

}

data class TaraguchiBranchingSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService
) : TaraguchiOpeningSession, BranchingStageOpeningSession {

    override fun branch(makeOffer: Boolean): TaraguchiOpeningSession =
        if (makeOffer)
            TaraguchiOfferStageSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state, expireService = this.expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                moveCandidates = emptySet(),
                symmetryMoves = emptySet()
            )
        else
            TaraguchiSwapStageSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state, expireService = this.expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                isBranched = true
            )

}

data class TaraguchiOfferStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
    override val symmetryMoves: Set<Pos>
) : TaraguchiOpeningSession, OfferStageOpeningSession {

    override val remainingMoves get() = 10 - this.moveCandidates.size

    override fun add(move: Pos): NegotiateStageOpeningSession =
        if (this.moveCandidates.size < 9)
            this.copy(
                moveCandidates = this.moveCandidates + move,
                symmetryMoves = this.symmetryMoves + this.calculateSymmetryMoves(move),
                messageBufferKey = MessageBufferKey.issue(),
            )
        else
            TaraguchiSelectStageSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state, expireService = this.expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                moveCandidates = this.moveCandidates + move,
            )

}

data class TaraguchiSelectStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
) : TaraguchiOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
            state = this.state.play(move),
            gameResult = this.gameResult,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.TARAGUCHI_10,
            expireService = this.expireService.next()
        )

}
