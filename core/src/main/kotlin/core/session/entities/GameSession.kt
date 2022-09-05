package core.session.entities

import core.assets.User
import core.assets.aiUser
import core.inference.AiLevel
import core.session.GameResult
import core.session.SessionManager
import jrenju.Board
import jrenju.notation.Pos
import jrenju.protocol.SolutionNode
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

    val messageBufferKey: String

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

}

data class AiGameSession(
    val aiLevel: AiLevel,
    val solution: Option<SolutionNode>,
    override val owner: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos?>,
    override val messageBufferKey: String,
    override val expireOffset: Long,
    override val recording: Boolean,
    override val expireDate: LinuxTime,
    override val createDate: LinuxTime = LinuxTime()
) : GameSession {

    override val opponent = aiUser

}

data class PvpGameSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos?>,
    override val messageBufferKey: String,
    override val expireOffset: Long,
    override val recording: Boolean,
    override val expireDate: LinuxTime,
    override val createDate: LinuxTime = LinuxTime()
) : GameSession

fun GameSession.nextWith(
    board: Board,
    move: Pos,
    gameResult: Option<GameResult>,
    messageBufferKey: String = SessionManager.generateMessageBufferKey(this.owner)
) = when (this) {
    is AiGameSession -> this.copy(
        board = board,
        history = this.history + move,
        gameResult = gameResult,
        expireDate = LinuxTime.withOffset(this.expireOffset),
        messageBufferKey = messageBufferKey
    )
    is PvpGameSession -> this.copy(
        board = board,
        history = this.history + move,
        gameResult = gameResult,
        expireDate = LinuxTime.withOffset(this.expireOffset),
        messageBufferKey = messageBufferKey
    )
}
