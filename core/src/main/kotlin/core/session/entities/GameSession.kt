package core.session.entities

import core.assets.User
import core.assets.aiUser
import core.session.GameResult
import jrenju.Board
import jrenju.notation.Color
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
    
    abstract val history: List<Pos>
    abstract val messageBufferKey: String

}

data class AiGameSession(
    override val owner: User,
    override val opponent: User,
    val solution: Option<SolutionNode>,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos>,
    override val messageBufferKey: String,
    override val expireDate: LinuxTime,
) : GameSession(expireDate)

data class PvpGameSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val board: Board,
    override val gameResult: Option<GameResult> = Option.Empty,
    override val history: List<Pos>,
    override val messageBufferKey: String,
    override val expireDate: LinuxTime,
) : GameSession(expireDate) {

    val priorPlayer get() =
        if (this.board.color() == Color.BLACK() && this.ownerHasBlack)
            this.owner
        else
            this.opponent

    val player get() =
        if (this.board.nextColor() == Color.BLACK() && this.ownerHasBlack)
            this.owner
        else
            this.opponent

}

fun generateMessageBufferKey(owner: User) =
    String(owner.name.toCharArray() + System.currentTimeMillis().toString().toCharArray())

fun GameSession.nextWith(board: Board, pos: Pos, gameResult: Option<GameResult> = Option.Empty) =
    when (this) {
        is AiGameSession -> this.copy(
            board = board, history = this.history + pos, gameResult = gameResult,
            messageBufferKey = generateMessageBufferKey(this.owner)
        )
        is PvpGameSession -> this.copy(
            board = board, history = this.history + pos, gameResult = gameResult,
            messageBufferKey = generateMessageBufferKey(this.owner)
        )
    }

fun GameSession.asResigned() =
    when (this) {
        is AiGameSession -> {
            val result = GameResult.Win(this.board.nextColor(), aiUser, this.owner)

            this.copy(
                gameResult = Option.Some(GameResult.Win(this.board.nextColor(), aiUser, this.owner))
            ) to result
        }
        is PvpGameSession -> {
            val (winner, looser) = if (this.board.color() == Color.BLACK() && this.ownerHasBlack)
                this.owner to this.opponent
            else
                this.opponent to this.owner

            val result = GameResult.Win(this.board.nextColor(), winner, looser)

            this.copy(gameResult = Option.Some(result)) to result
        }
    }
