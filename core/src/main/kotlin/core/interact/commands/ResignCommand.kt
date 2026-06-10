package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.MessageManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import renju.notation.GameResult

class ResignCommand(private val sessionId: SessionId) : Command {

    override val name = "resign"

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
        var session: GameSession? = null
        var result: GameResult.Win? = null

        val finishedSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            session = currentSession
            val (finishedSession, finishedResult) = GameManager.resignSession(currentSession, GameResult.Cause.RESIGN, user)
            result = finishedResult
            finishedSession
        } as RenjuSession

        val sessionValue = session ?: throw IllegalStateException()
        val resultValue = result ?: throw IllegalStateException()

        GameManager.finishSession(bot, channel, finishedSession, resultValue)

        val publisher = run {
            val boardMessage = MessageManager.viewHeadMessage(bot.sessions, sessionValue.messageBufferKey)

            if (config.swapType == SwapType.EDIT && boardMessage != null)
                publishers.edit(boardMessage)
            else
                publishers.plain
        }

        val io = effect {
            when (finishedSession) {
                is EngineGameSession ->
                    service.buildSurrenderedPVE(publishers.plain, config.language.container, finishedSession.humanPlayer)
                is PvpGameSession, is OpeningSession ->
                    service.buildSurrenderedPVP(publishers.plain, config.language.container, resultValue.winner, resultValue.loser)
            }.launch()()

            val finishOrders = buildFinishProcedure(bot, service, publisher, config, sessionValue, finishedSession)()

            finishOrders + Order.ArchiveSession(finishedSession, config.archivePolicy)
        }

        io to this.writeCommandReport("surrendered, terminate session by $resultValue", channel, user)
    }

}
