package core.session.entities

import core.assets.User
import renju.notation.GameResult

data class PvpGameSession(
    val context: GameSessionContext<User.Human>,
    override val gameResult: GameResult? = null,
    override val recording: Boolean,
) : UserSession, PlayGameSession {

    override val id = this.context.id
    override val expireService = this.context.expireService

    override val state = this.context.state
    override val users = this.context.users
    override val player = this.context.users[this.state.board.playerColor]
    override val opponent = this.context.users[!this.state.board.playerColor]

    override val messageBufferKey = this.context.messageBufferKey

    override val ruleKind = this.context.ruleKind

}
