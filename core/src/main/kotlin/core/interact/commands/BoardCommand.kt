package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import kotlin.time.Instant

class BoardCommand(
    private val sessionId: SessionId
) : Command {

    override val name = "board"

    override val responseFlag = ResponseFlag.Immediately

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
            buildBoardProcedure(config, service, publishers, runtime)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "reopen board", channel, user))
    }

}
