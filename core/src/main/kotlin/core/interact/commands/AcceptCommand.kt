package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.OpeningSession
import core.session.entities.SessionId
import utils.tuple

class AcceptCommand(
    private val requestSessionId: SessionId,
) : Command {

    override val name = "accept"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val requestSession = SessionManager.retrieveRequestSession(bot.sessions, this.requestSessionId).snapshot()
        if (requestSession.recipient.id != user.id) throw IllegalStateException()

        val session = PvpGameManager.create(requestSession.requester, requestSession.recipient, requestSession.rule)

        SessionManager.deleteRequestSession(bot.sessions, this.requestSessionId)
        SessionManager.insertGameSession(bot.sessions, channel, session)

        val guidePublisher = MessageManager.checkoutMessages(bot.sessions, requestSession.messageBufferKey)
            ?.let { publishers.edit(it.first()) }
            ?: publishers.plain

        val beginIO = when (session) {
            is OpeningSession ->
                service.buildBeginsOpening(guidePublisher, config.language.container, session.users, session.rule)
            else ->
                service.buildBeginsPvp(guidePublisher, config.language.container, session.users)
        }
            .launch()

        val boardIO = buildBoardProcedure(bot, config, service, publishers.plain, session)

        val io = effect {
            beginIO()
            boardIO()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("accept ${requestSession.requester}'s request", channel, user))
    }

}
