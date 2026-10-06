package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.*
import kotlin.time.Instant

class ResignCommand(
    private val sessionId: SessionId,
) : Command {

    override val name = "resign"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).interact { runtime ->
            val session = runtime.session
            check(session.users.black.id == user.id || session.users.white.id == user.id)
            runtime.session = when (session) {
                is PvpGameSession -> PvpGameManager.resign(session, user)
                is OpeningSession -> PvpGameManager.resign(session, user)
                is EngineGameSession -> EngineGameManager.resign(session, EngineGameManager.ResignCause.RESIGN)
            }
            SessionManager.finishGameSession(bot.sessions, runtime)
            buildFinishProcedure(bot, channel, config, service, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "resigned", channel, user))
    }

}
