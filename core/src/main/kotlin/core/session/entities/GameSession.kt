package core.session.entities

import arrow.core.Option
import core.assets.User
import core.session.Rule
import renju.Board
import renju.GameState
import renju.History
import renju.notation.GameResult
import kotlin.time.Instant

sealed interface GameSession : Expirable {

    val expireService: ExpireService

    override val expireDate: Instant get() = this.expireService.expireAt

    val owner: User
    val opponent: User
    val ownerHasBlack: Boolean

    val state: GameState

    val board: Board get() = this.state.board

    val history: History get() = this.state.history

    val gameResult: Option<GameResult>

    val messageBufferKey: MessageBufferKey

    val recording: Boolean

    val ruleKind: Rule

    val player get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.owner
        else -> this.opponent
    }

    val nextPlayer get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.opponent
        else -> this.owner
    }

    fun updateResult(gameResult: GameResult): RenjuSession

}
