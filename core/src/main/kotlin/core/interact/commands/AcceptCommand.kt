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
import core.session.GameManager
import core.session.MessageManager
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.OpeningSession
import core.session.entities.SessionId
import utils.lang.tuple

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
        messageRef: MessageRef,
        publishers: PublisherSet,
    ) = runCatching {
        val requestSession = SessionManager.retrieveRequestSession(bot.sessions, this.requestSessionId).snapshot()
        if (requestSession.opponent.id != user.id) throw IllegalStateException()

        val gameSession = GameManager.generatePvpSession(bot, requestSession.requester, requestSession.opponent, requestSession.rule)

        SessionManager.createGameSession(bot.sessions, channel, gameSession.participantIds, gameSession)
        SessionManager.deleteRequestSession(bot.sessions, this.requestSessionId)

        val guidePublisher = MessageManager.checkoutMessages(bot.sessions, requestSession.messageBufferKey)
            ?.let { publishers.edit(it.first()) }
            ?: publishers.plain

        val beginIO = when (gameSession) {
            is OpeningSession ->
                service.buildBeginsOpening(guidePublisher, config.language.container, gameSession.user, gameSession.ruleKind)
            else ->
                service.buildBeginsPvp(guidePublisher, config.language.container, gameSession.user)
        }
            .launch()

        val boardIO = buildBoardProcedure(bot, config, service, publishers.plain, gameSession)

        val io = effect {
            beginIO()
            boardIO()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("accept ${requestSession.requester}'s request", channel, user))
    }

}
