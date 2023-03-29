package core.session.entities

import core.assets.User
import core.assets.aiUser
import core.inference.AiLevel
import core.inference.Token
import core.session.Rule
import renju.Board
import renju.notation.Pos
import renju.protocol.SolutionNode
import utils.structs.Option

sealed interface RenjuSession : GameSession {

    fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey): RenjuSession

}

data class AiGameSession(
    val aiLevel: AiLevel,
    val solution: Option<SolutionNode>,
    val resRenjuToken: Token,
    override val owner: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val recording: Boolean,
    override val ruleKind: Rule,
    override val expireService: ExpireService
) : RenjuSession {

    override val opponent = aiUser

    override fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireService = this.expireService.next(),
            messageBufferKey = messageBufferKey
        )

    override fun updateResult(gameResult: GameResult) = this.copy(gameResult = Option.Some(gameResult))

}

data class PvpGameSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val recording: Boolean,
    override val ruleKind: Rule,
    override val expireService: ExpireService
) : RenjuSession {

    override fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireService = this.expireService.next(),
            messageBufferKey = messageBufferKey
        )

    override fun updateResult(gameResult: GameResult) = this.copy(gameResult = Option.Some(gameResult))

}
