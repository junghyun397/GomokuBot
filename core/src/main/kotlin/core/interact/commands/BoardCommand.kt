package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import utils.lang.tuple

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
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet,
    ) = runCatching {
        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).snapshot()

        val io = effect {
            buildBoardProcedure(bot, channel, config, service, publishers.plain, session)()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("reopen board", channel, user))
    }

}
