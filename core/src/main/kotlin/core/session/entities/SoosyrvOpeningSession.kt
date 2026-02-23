package core.session.entities

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.User
import core.session.Rule
import renju.Board
import renju.notation.Pos

sealed interface SoosyrvOpeningSession : OpeningSession {

    override val ruleKind get() = Rule.SOOSYRV_8

}

data class SoosyrvMoveStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val isBranched: Boolean
) : SoosyrvOpeningSession, MoveStageOpeningSession {

    override fun validateMove(move: Pos) = this.inSquare(move)

    override fun inSquare(move: Pos): Boolean =
        this.isBranched ||
                6 - board.moves() < move.row() && move.row() < 8 + board.moves() &&
                6 - board.moves() < move.col() && move.col() < 8 + board.moves()

    override val player get() =
        if (this.board.moves() < 2)
            if (this.ownerHasBlack) this.owner
            else this.opponent
        else super<MoveStageOpeningSession>.player

    override val nextPlayer get() =
        if (this.board.moves() < 2) this.player
        else super<MoveStageOpeningSession>.nextPlayer

    override fun next(move: Pos): SoosyrvOpeningSession =
        if (this.isBranched)
            SoosyrvDeclareStageOpeningSession(
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                board = this.board.set(move),
                history = this.history + move,
            )
        else if (this.board.moves() < 2)
            this.copy(
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                board = this.board.set(move),
                history = this.history + move,
            )
        else // moves == 3
            SoosyrvSwapStageSession(
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                board = this.board.set(move),
                history = this.history + move,
                offerCount = None
            )

}

data class SoosyrvSwapStageSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val offerCount: Option<Int>
) : SoosyrvOpeningSession, SwapStageOpeningSession {

    override val isBranched = true

    override fun swap(doSwap: Boolean): SoosyrvOpeningSession =
        this.offerCount.fold(
            ifEmpty = {
                SoosyrvMoveStageSession(
                    owner = this.owner, opponent = this.opponent,
                    ownerHasBlack = this.ownerHasBlack xor doSwap,
                    messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                    board = this.board, history = this.history,
                    isBranched = true
                )
            },
            ifSome = { count ->
                SoosyrvOfferStageOpeningSession(
                    owner = this.owner, opponent = this.opponent,
                    ownerHasBlack = this.ownerHasBlack xor doSwap,
                    messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
                    board = this.board, history = this.history,
                    moveCandidates = emptySet(),
                    symmetryMoves = emptySet(),
                    offerCount = count
                )
            }
        )

}

data class SoosyrvDeclareStageOpeningSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
) : SoosyrvOpeningSession, DeclareStageOpeningSession {

    override val player get() = super<DeclareStageOpeningSession>.nextPlayer

    override val nextPlayer get() = super<DeclareStageOpeningSession>.player

    override val maxOfferCount = 8

    override fun declare(count: Int) =
        SoosyrvSwapStageSession(
            owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
            board = this.board, history = this.history,
            messageBufferKey = MessageBufferKey.issue(), expireService = this.expireService.next(),
            offerCount = Some(count)
        )

}

data class SoosyrvOfferStageOpeningSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val history: List<Pos?>,
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
                owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
                board = this.board, history = this.history,
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
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val expireService: ExpireService,
    override val moveCandidates: Set<Pos>,
) : SoosyrvOpeningSession, SelectStageOpeningSession {

    override fun select(move: Pos): PvpGameSession =
        PvpGameSession(
            owner = this.owner, opponent = this.opponent, ownerHasBlack = this.ownerHasBlack,
            board = this.board.set(move),
            gameResult = this.gameResult,
            history = this.history + move,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.SOOSYRV_8,
            expireService = this.expireService.next()
        )

}
