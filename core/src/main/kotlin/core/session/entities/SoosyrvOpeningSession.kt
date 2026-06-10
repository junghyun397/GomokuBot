package core.session.entities

import core.assets.User
import core.session.Rule
import renju.GameState
import renju.notation.Pos

sealed interface SoosyrvOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.SOOSYRV_8

}

data class SoosyrvMoveStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val isBranched: Boolean
) : SoosyrvOpeningSession, MoveStageOpeningSession {

    override fun validateMove(move: Pos) = this.inSquare(move)

    override fun inSquare(move: Pos): Boolean =
        this.isBranched ||
                6 - history.moves < move.row && move.row < 8 + history.moves &&
                6 - history.moves < move.col && move.col < 8 + history.moves

    override val player get() =
        if (this.history.moves < 2)
            this.blackPlayer
        else super<MoveStageOpeningSession>.player

    override val nextPlayer get() =
        if (this.history.moves < 2) this.player
        else super<MoveStageOpeningSession>.nextPlayer

    override fun next(move: Pos): SoosyrvOpeningSession =
        if (this.isBranched)
            SoosyrvDeclareStageOpeningSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                state = this.state.play(move),
            )
        else if (this.history.moves < 2)
            this.copy(
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                state = this.state.play(move),
            )
        else // moves == 3
            SoosyrvSwapStageSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                state = this.state.play(move),
                offerCount = null
            )

}

data class SoosyrvSwapStageSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val offerCount: Int?
) : SoosyrvOpeningSession, SwapStageOpeningSession {

    override val isBranched = true

    override fun swap(doSwap: Boolean): SoosyrvOpeningSession =
        if (this.offerCount == null)
            SoosyrvMoveStageSession(
                id = this.id,
                blackPlayer = if (doSwap) this.whitePlayer else this.blackPlayer,
                whitePlayer = if (doSwap) this.blackPlayer else this.whitePlayer,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                state = this.state,
                isBranched = true
            )
        else
            SoosyrvOfferStageOpeningSession(
                id = this.id,
                blackPlayer = if (doSwap) this.whitePlayer else this.blackPlayer,
                whitePlayer = if (doSwap) this.blackPlayer else this.whitePlayer,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                state = this.state,
                moveCandidates = emptySet(),
                symmetryMoves = emptySet(),
                offerCount = this.offerCount
            )

}

data class SoosyrvDeclareStageOpeningSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
) : SoosyrvOpeningSession, DeclareStageOpeningSession {

    override val player get() = super<DeclareStageOpeningSession>.nextPlayer

    override val nextPlayer get() = super<DeclareStageOpeningSession>.player

    override val maxOfferCount = 8

    override fun declare(count: Int) =
        SoosyrvSwapStageSession(
            id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
            state = this.state,
            messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
            offerCount = count
        )

}

data class SoosyrvOfferStageOpeningSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
    override val symmetryMoves: Set<Pos>,
    val offerCount: Int
) : SoosyrvOpeningSession, OfferStageOpeningSession {

    override val remainingMoves get() = offerCount - this.moveCandidates.size

    override fun add(move: Pos): NegotiateStageOpeningSession =
        if (this.remainingMoves == 1)
            SoosyrvSelectStageOpeningSession(
                id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
                state = this.state,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                moveCandidates = this.moveCandidates + move
            )
        else
            this.copy(
                moveCandidates = this.moveCandidates + move,
                symmetryMoves = this.symmetryMoves + this.calculateSymmetryMoves(move),
                messageBufferKey = MessageBufferKey.issue(),
            )

}

data class SoosyrvSelectStageOpeningSession(
    override val id: SessionId,
    override val blackPlayer: User,
    override val whitePlayer: User,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
) : SoosyrvOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            id = this.id, blackPlayer = this.blackPlayer, whitePlayer = this.whitePlayer,
            state = this.state.play(move),
            gameResult = this.gameResult,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.SOOSYRV_8,
            expireService = this.expireService.next()
        )

}
