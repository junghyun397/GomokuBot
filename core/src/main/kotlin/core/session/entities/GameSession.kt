package core.session.entities

import core.assets.User
import core.assets.aiUser
import core.inference.AiLevel
import core.session.GameResult
import core.session.Token
import renju.Board
import renju.notation.Pos
import renju.protocol.SolutionNode
import utils.assets.LinuxTime
import utils.structs.Option

sealed interface GameSession : Expirable {

    val createDate: LinuxTime

    override val expireDate: LinuxTime

    val owner: User
    val opponent: User
    val ownerHasBlack: Boolean

    val board: Board

    val gameResult: Option<GameResult>

    val history: List<Pos?>

    val messageBufferKey: MessageBufferKey

    val recording: Boolean

    val expireOffset: Long

    val player get() = when {
        this.ownerHasBlack xor !this.board.isNextColorBlack -> this.owner
        else -> this.opponent
    }

    val nextPlayer get() = when {
        this.ownerHasBlack xor !this.board.isNextColorBlack -> this.opponent
        else -> this.owner
    }

    fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey): GameSession

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
    override val expireOffset: Long,
    override val recording: Boolean,
    override val expireDate: LinuxTime,
    override val createDate: LinuxTime = LinuxTime.now()
) : GameSession {

    override val opponent = aiUser

    override fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireDate = LinuxTime.nowWithOffset(this.expireOffset),
            messageBufferKey = messageBufferKey
        )

}

data class PvpGameSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos?>,
    override val messageBufferKey: MessageBufferKey,
    override val expireOffset: Long,
    override val recording: Boolean,
    override val expireDate: LinuxTime,
    override val createDate: LinuxTime = LinuxTime.now()
) : GameSession {

    override fun next(board: Board, move: Pos, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireDate = LinuxTime.nowWithOffset(this.expireOffset),
            messageBufferKey = messageBufferKey
        )

}
