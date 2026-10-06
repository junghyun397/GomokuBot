package core.interact.commands

import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import core.session.entities.SwapStageOpeningSession
import kotlin.time.Instant

class OpeningSwapCommand(
    private val sessionId: SessionId,
    private val doSwap: Boolean,
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.DeferEdit

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(this.sessionId).interact { runtime ->
            val session = runtime.session as? SwapStageOpeningSession ?: throw IllegalStateException()
            check(session.player.id == user.id)
            runtime.session = session.swap(this.doSwap)
            buildUpdateBoardProcedure(config, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "make swap ${this.doSwap}", channel, user))
    }

}
