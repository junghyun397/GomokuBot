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
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import kotlin.time.Instant

class OpeningBranchingCommand(
    private val sessionId: SessionId,
    private val takeBranch: Boolean,
) : Command {

    override val name = "opening-branching"

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
            val session = runtime.session as? BranchingStageOpeningSession ?: throw IllegalStateException()
            check(session.player.id == user.id)
            runtime.session = session.branch(this.takeBranch)
            buildUpdateBoardProcedure(config, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "has chosen ${this.takeBranch}", channel, user))
    }

}
