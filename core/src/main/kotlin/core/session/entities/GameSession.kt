package core.session.entities

import arrow.core.Option
import core.assets.User
import core.assets.humanId
import core.session.Rule
import renju.Board
import renju.GameState
import renju.History
import renju.notation.GameResult
import kotlin.time.Instant

sealed interface GameSession : Expirable {

    val id: SessionId

    val expireService: ExpireService

    override val expireDate: Instant get() = this.expireService.expireAt

    val blackPlayer: User
    val whitePlayer: User

    val owner: User get() = this.blackPlayer
    val opponent: User get() = this.whitePlayer

    val state: GameState

    val board: Board get() = this.state.board

    val history: History get() = this.state.history

    val gameResult: Option<GameResult>

    val messageBufferKey: MessageBufferKey

    val recording: Boolean

    val ruleKind: Rule

    val currentPlayer get() =
        if (this.board.isNextColorBlack) this.blackPlayer
        else this.whitePlayer

    val player get() = this.currentPlayer

    val nextPlayer get() =
        if (this.board.isNextColorBlack) this.whitePlayer
        else this.blackPlayer

    val participantIds get() = setOfNotNull(this.blackPlayer.humanId, this.whitePlayer.humanId)

    fun updateResult(gameResult: GameResult): RenjuSession

}
