package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.database.DatabaseConnection
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionPool
import core.session.entities.*
import kotlin.time.Instant

class ExpireGameCommand(
    private val runtime: SessionRuntime<GameSession>,
) : InternalCommand {

    override val name = "expire-game"

    context(dbConnection: DatabaseConnection, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        val io = effect {
            val runtime = this@ExpireGameCommand.runtime
            val session = runtime.session

            runtime.session = when (session) {
                is PvpGameSession -> PvpGameManager.resign(session, null)
                is OpeningSession -> PvpGameManager.resign(session, null)
                is EngineGameSession -> EngineGameManager.resign(session, EngineGameManager.ResignCause.TIMEOUT)
            }

            buildFinishProcedure(config, channel, publisher, runtime).bind()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "expired ${this.runtime.session.id}", channel))
    }

}
