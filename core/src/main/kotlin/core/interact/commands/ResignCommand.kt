package core.interact.commands

import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.*
import kotlin.time.Instant

class ResignCommand(
    private val sessionId: SessionId,
) : Command {

    override val name = "resign"

    override val responseFlag = ResponseFlag.Defer

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(this.sessionId).interact { runtime ->
            val session = runtime.session
            check(session.users.black.id == user.id || session.users.white.id == user.id)
            runtime.session = when (session) {
                is PvpGameSession -> PvpGameManager.resign(session, user)
                is OpeningSession -> PvpGameManager.resign(session, user)
                is EngineGameSession -> EngineGameManager.resign(session, EngineGameManager.ResignCause.RESIGN)
            }
            SessionManager.finishGameSession(runtime)
            buildFinishProcedure(config, channel, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "resigned", channel, user))
    }

}
