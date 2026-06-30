package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.*
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
        service: PlatformService,
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

        SessionManager.deleteGameSession(bot.sessions, this.sessionId)

        StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

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
                    service.buildMessage(
                        publishers.plain,
                        PlatformMessage(config.language.container.endEngineResign(service.formatUser(session.humanPlayer)))
                    )
                is PvpGameSession, is OpeningSession -> {
                    service.buildMessage(
                        publishers.plain,
                        PlatformMessage(config.language.container.endPvpResign(
                            service.formatUser(session.users[result.winner!!]),
                            service.formatUser(session.users[!result.winner!!])
                        ))
                    )
                }
            }.launch()()

            buildFinishProcedure(bot, service, publisher, config, session, messageBufferKey)()

            service.archiveSession(session, config.archivePolicy)
        }

        io to this.writeCommandReport("surrendered, terminate session by $result", channel, user)
    }

}
