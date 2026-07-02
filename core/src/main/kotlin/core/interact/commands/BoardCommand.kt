package core.interact.commands

import arrow.core.raise.effect
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
        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).snapshot()

        val io = effect {
            buildBoardProcedure(bot, config, service, publishers.plain, session)()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "reopen board", channel, user))
    }

}
