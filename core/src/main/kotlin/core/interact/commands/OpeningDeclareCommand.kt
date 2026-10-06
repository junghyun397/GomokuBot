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
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.SessionId
import kotlin.time.Instant

class OpeningDeclareCommand(
    private val sessionId: SessionId,
    private val maxOfferCount: Int,
) : Command {

    override val name = "opening-declare"

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
            val session = runtime.session as? DeclareStageOpeningSession ?: throw IllegalStateException()
            check(session.player.id == user.id)
            check(this.maxOfferCount in 1 .. session.maxOfferCount)
            runtime.session = session.declare(this.maxOfferCount)
            buildUpdateBoardProcedure(config, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "declare 5th moves ${this.maxOfferCount}", channel, user))
    }

}
