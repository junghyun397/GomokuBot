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

sealed class GameSession(
    override val expireDate: LinuxTime,
) : Expirable {

    abstract val owner: User
    abstract val opponent: User
    abstract val ownerHasBlack: Boolean

    abstract val board: Board

    abstract val gameResult: Option<GameResult>

    abstract val history: List<Pos?>

    abstract val messageBufferKey: String

    abstract val expireOffset: Long

    val player get() =
        if (this.ownerHasBlack xor !this.board.isNextColorBlack)
            this.owner
        else
            this.opponent

    val nextPlayer get() =
        if (this.ownerHasBlack xor !this.board.isNextColorBlack)
            this.opponent
        else
            this.owner

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
    override val expireDate: LinuxTime,
) : GameSession(expireDate) {

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
    override val expireDate: LinuxTime,
) : GameSession(expireDate)

fun GameSession.nextWith(
    board: Board,
    move: Pos,
    gameResult: Option<GameResult>,
    messageBufferKey: String = SessionManager.generateMessageBufferKey(this.owner)
) =
    when (this) {
        is AiGameSession -> this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireDate = LinuxTime.withExpireOffset(this.expireOffset),
            messageBufferKey = messageBufferKey
        )
        is PvpGameSession -> this.copy(
            board = board,
            history = this.history + move,
            gameResult = gameResult,
            expireDate = LinuxTime.withExpireOffset(this.expireOffset),
            messageBufferKey = messageBufferKey
        )
    }
