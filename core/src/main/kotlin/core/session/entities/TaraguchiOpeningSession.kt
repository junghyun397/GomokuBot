package core.session.entities

import arrow.core.None
import core.assets.User
import core.session.Rule
import renju.GameState
import renju.notation.Pos

sealed interface TaraguchiOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.TARAGUCHI_10

}

data class TaraguchiSwapStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val isBranched: Boolean
) : TaraguchiOpeningSession, SwapStageOpeningSession {

    override val offerCount = None

    override fun swap(doSwap: Boolean): GameSession =
        if (this.isBranched && this.history.moves == 5)
            PvpGameSession(
                owner = this.owner, opponent = this.opponent,
                ownerHasBlack = this.ownerHasBlack xor doSwap,
                state = this.state,
                gameResult = this.gameResult,
                messageBufferKey = MessageBufferKey.issue(),
                recording = true,
                ruleKind = Rule.TARAGUCHI_10,
                expireService = expireService.next()
            )
        else
            TaraguchiMoveStageSession(
                owner = this.owner, opponent = this.opponent,
                ownerHasBlack = this.ownerHasBlack xor doSwap,
                state = this.state,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next(),
                isBranched = this.isBranched
            )

}

data class TaraguchiMoveStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
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
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                state = this.state.play(move),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next()
            )
        else
            TaraguchiSwapStageSession(
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                state = this.state.play(move),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = this.expireService.next(),
                isBranched = this.isBranched
            )

}

data class TaraguchiBranchingSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService
) : TaraguchiOpeningSession, BranchingStageOpeningSession {

    override fun branch(makeOffer: Boolean): TaraguchiOpeningSession =
        if (makeOffer)
            TaraguchiOfferStageSession(
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                state = this.state, expireService = this.expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                moveCandidates = emptySet(),
                symmetryMoves = emptySet()
            )
        else
            TaraguchiSwapStageSession(
                owner = this.owner, opponent = this.opponent, ownerHasBlack = ownerHasBlack,
                state = this.state, expireService = expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                isBranched = true
            )

}

data class TaraguchiOfferStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
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
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                state = this.state, expireService = this.expireService.next(),
                messageBufferKey = MessageBufferKey.issue(),
                moveCandidates = this.moveCandidates + move,
            )

}

data class TaraguchiSelectStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val state: GameState,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
) : TaraguchiOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
            state = this.state.play(move),
            gameResult = this.gameResult,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.TARAGUCHI_10,
            expireService = this.expireService.next()
        )

}
