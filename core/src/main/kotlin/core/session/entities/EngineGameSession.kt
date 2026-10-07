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
    val engineState: Either<Pair<GameResult, EloRating.Delta>, MintakaSession>,
    val userColor: Color,
    val engineLevel: EngineLevel,
    val userRating: EloRating,
    override val recording: Boolean,
    val remainingUndos: Int = 5,
) : PlayGameSession {

    val mintakaSession: MintakaSession? get() = this.engineState.getOrNull()
    val terminalState get() = this.engineState.leftOrNull()
    override val gameResult get() = this.terminalState?.first
    val ratingDelta get() = this.terminalState?.second

    override val id = this.context.id
    override val expireService = this.context.expireService

    override val state = this.context.state
    override val users = this.context.users

    override val rule = this.context.ruleKind

    val humanPlayer get() = this.context.requester

}
