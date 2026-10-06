package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.entities.*
import kotlin.time.Instant

class ExpireGameCommand(
    private val runtime: SessionRuntime<GameSession>,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
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

            buildFinishProcedure(bot, channel, config, service, publisher, runtime)()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "expired ${this.runtime.session.id}", channel))
    }

}
