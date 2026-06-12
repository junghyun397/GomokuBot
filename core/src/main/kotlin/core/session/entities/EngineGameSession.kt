package core.session.entities

import core.assets.User
import renju.GameState
import renju.notation.GameResult

data class EngineGameSession(
    override val context: GameSessionContext,
    val mintakaSession: MintakaSession,
    val humanPlayer: User,
    override val gameResult: GameResult? = null,
    override val recording: Boolean,
    override val ruleKind: Rule,
) : RenjuSession {

    override fun next(state: GameState, gameResult: GameResult?, messageBufferKey: MessageBufferKey) =
        this.copy(
            context = this.context.next(
                state = state,
                messageBufferKey = messageBufferKey,
            ),
            gameResult = gameResult,
        )

    override fun anonymous(): EngineGameSession =
        this.copy(
            context = this.context.copy(user = this.user.map { it.anonymous() }),
            humanPlayer = this.humanPlayer.anonymous(),
            gameResult = this.gameResult.anonymous(),
        )

}
