package core.session.entities

import renju.GameState
import renju.notation.GameResult

data class PvpGameSession(
    override val context: GameSessionContext,
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

    override fun anonymous(): PvpGameSession =
        this.copy(
            context = this.context.copy(user = this.user.map { it.anonymous() }),
            gameResult = this.gameResult.anonymous(),
        )

}
