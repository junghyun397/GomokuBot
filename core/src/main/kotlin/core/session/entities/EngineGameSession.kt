package core.session.entities

import arrow.core.Either
import core.assets.User
import core.engine.EloRating
import core.engine.EngineLevel
import core.engine.MintakaServer
import core.engine.MintakaSession
import renju.notation.Color
import renju.notation.GameResult

data class EngineGameSession(
    val context: GameSessionContext<User>,
    val mintakaServer: MintakaServer,
    val engineState: Either<GameResult, MintakaSession>,
    val userColor: Color,
    val engineLevel: EngineLevel,
    val userRating: EloRating,
    override val recording: Boolean,
) : PlayGameSession {

    val mintakaSession: MintakaSession? get() = this.engineState.getOrNull()
    override val gameResult get() = this.engineState.leftOrNull()

    override val id = this.context.id
    override val expireService = this.context.expireService

    override val state = this.context.state
    override val users = this.context.users

    override val messageBufferKey = this.context.messageBufferKey

    override val rule = this.context.ruleKind

    val humanPlayer get() = this.context.requester

}
