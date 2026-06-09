package core.interact.commands

import core.session.MessageManager
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

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val requestSession = SessionManager.retrieveRequestSession(bot.sessions, this.requestSessionId).snapshot()
        if (requestSession.opponent.id != user.id) throw IllegalStateException()

        val gameSession = GameManager.generatePvpSession(bot, requestSession.owner, requestSession.opponent, requestSession.rule)

        SessionManager.createGameSession(bot.sessions, channel, gameSession.participantIds, gameSession)
        SessionManager.deleteRequestSession(bot.sessions, this.requestSessionId)

        val guidePublisher = MessageManager.checkoutMessages(bot.sessions, requestSession.messageBufferKey)
            ?.let { publishers.edit(it.first()) }
            ?: publishers.plain

        val beginIO = when (gameSession) {
            is OpeningSession ->
                service.buildBeginsOpening(guidePublisher, config.language.container, gameSession.nextPlayer, gameSession.player, gameSession.ruleKind)
            else ->
                service.buildBeginsPVP(guidePublisher, config.language.container, gameSession.player, gameSession.nextPlayer)
        }
            .launch()

        val boardIO = buildBoardProcedure(bot, channel, config, service, publishers.plain, gameSession)

        val io = effect {
            beginIO()
            boardIO()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("accept ${requestSession.owner}'s request", channel, user))
    }

}
