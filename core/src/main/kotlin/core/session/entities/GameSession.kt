package core.session.entities

import core.assets.User
import renju.GameState
import renju.notation.ColorContainer
import renju.notation.GameResult
import renju.notation.Pos
import kotlin.time.Instant

data class GameSessionContext<T: User>(
    val id: SessionId,
    val requester: User.Human,
    val users: ColorContainer<T>,
    val state: GameState,
    val messageBufferKey: MessageBufferKey,
    val expireService: ExpireService,
    val ruleKind: Rule,
) {

    fun next(
        state: GameState = this.state,
        users: ColorContainer<T> = this.users,
        messageBufferKey: MessageBufferKey = MessageBufferKey.issue(),
        expireService: ExpireService = this.expireService.next(),
    ): GameSessionContext<T> =
        this.copy(
            users = users,
            state = state,
            messageBufferKey = messageBufferKey,
            expireService = expireService,
        )

}

sealed interface GameSession : Expirable {

    val id: SessionId
    val expireService: ExpireService
    override val expireDate: Instant get() = this.expireService.expireAt
    val recording: Boolean

    val state: GameState
    val gameResult: GameResult?

    val users: ColorContainer<User>
    val player get() = this.users[this.state.board.playerColor]
    val opponent get() = this.users[!this.state.board.playerColor]

    val messageBufferKey: MessageBufferKey

    val rule: Rule

    fun isLegalMove(move: Pos): Boolean = this.state.board.isLegalMove(move)

}

sealed interface PlayGameSession : GameSession

sealed interface UserSession : GameSession {

    override val users: ColorContainer<User.Human>

}
