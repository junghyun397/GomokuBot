package core.session.entities

import core.assets.User
import core.assets.humanId
import renju.GameState
import renju.notation.ColorContainer
import renju.notation.GameResult
import kotlin.time.Instant

data class GameSessionContext(
    val id: SessionId,
    val user: ColorContainer<User>,
    val state: GameState,
    val messageBufferKey: MessageBufferKey,
    val expireService: ExpireService,
) {

    fun next(
        user: ColorContainer<User> = this.user,
        state: GameState = this.state,
        messageBufferKey: MessageBufferKey = MessageBufferKey.issue(),
        expireService: ExpireService = this.expireService.next(),
    ): GameSessionContext =
        this.copy(
            user = user,
            state = state,
            messageBufferKey = messageBufferKey,
            expireService = expireService,
        )

}

sealed interface GameSession : Expirable {

    val context: GameSessionContext

    val id get() = this.context.id
    val expireService get() = this.context.expireService
    override val expireDate: Instant get() = this.expireService.expireAt
    val recording: Boolean

    val ruleKind: Rule
    val state get() = this.context.state
    val gameResult: GameResult?

    val user get() = this.context.user
    val player get() = this.user[this.state.board.playerColor]
    val opponent get() = this.user[!this.state.board.playerColor]

    val messageBufferKey get() = this.context.messageBufferKey
    val participantIds get() = setOfNotNull(this.user.black.humanId, this.user.white.humanId)

    fun updateResult(gameResult: GameResult): RenjuSession

}

sealed interface RenjuSession : GameSession {

    fun next(state: GameState, gameResult: GameResult?, messageBufferKey: MessageBufferKey): RenjuSession

    fun anonymous(): RenjuSession

}

internal fun User.anonymous(): User =
    when (this) {
        User.GomokuBot -> User.GomokuBot
        is User.Human, User.Anonymous -> User.Anonymous
    }

internal fun GameResult?.anonymous(): GameResult? =
    when (this) {
        is GameResult.Win -> this.copy(
            winner = this.winner.anonymous(),
            loser = this.loser.anonymous(),
        )
        else -> this
    }
