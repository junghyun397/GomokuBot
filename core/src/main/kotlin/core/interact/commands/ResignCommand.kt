package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.EngineGameManager
import core.session.MessageManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.*
import utils.tuple

class ResignCommand(
    private val sessionId: SessionId,
) : Command {

    override val name = "resign"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val (session, messageBufferKey) = run {
            val staleSession = SessionManager.deleteGameSession(bot.sessions, this.sessionId)!!

            val finishedSession = when (staleSession) {
                is PvpGameSession -> PvpGameManager.resign(staleSession, user)
                is OpeningSession -> PvpGameManager.resign(staleSession, user)
                is EngineGameSession -> EngineGameManager.resign(staleSession, EngineGameManager.ResignCause.RESIGN)
            }

            tuple(finishedSession, staleSession.messageBufferKey)
        }

        val result = session.gameResult!!

        session.extractGameRecord(channel.id)?.let { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }

        val publisher = run {
            val boardMessage = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            if (config.swapType == SwapType.EDIT && boardMessage != null)
                publishers.edit(boardMessage)
            else
                publishers.plain
        }

        val io = effect {
            when (session) {
                is EngineGameSession ->
                    service.buildResignedEngine(publishers.plain, config.language.container, session.humanPlayer)
                is PvpGameSession, is OpeningSession -> {
                    service.buildResignedPvp(
                        publishers.plain, config.language.container,
                        session.users[result.winner!!], session.users[!result.winner!!]
                    )
                }
            }.launch()()

            val finishOrders = buildFinishProcedure(bot, service, publisher, config, session, messageBufferKey)()

            finishOrders + Order.ArchiveSession(session, config.archivePolicy)
        }

        io to this.writeCommandReport("surrendered, terminate session by $result", channel, user)
    }

}
