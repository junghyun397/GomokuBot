package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.*
import core.session.entities.*
import utils.replaceIf
import utils.tuple

class ExpireGameCommand(
    private val session: GameSession,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
    ) = runCatching {
        val session = when (this.session) {
            is PvpGameSession -> PvpGameManager.resign(this.session, null)
            is EngineGameSession -> EngineGameManager.resign(this.session, EngineGameManager.ResignCause.TIMEOUT)
            is OpeningSession -> PvpGameManager.resign(this.session, null)
        }

        SessionManager.deleteGameSession(bot.sessions, this.session.id)

        StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

        val io = if (publisher != null) {
            effect {
                val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

                val noticePublisher = publisher.plain

                val boardPublisher = noticePublisher
                    .replaceIf(config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

                val messageBufferKey = session.messageBufferKey

                when (session) {
                    is PvpGameSession, is OpeningSession -> service.buildMessage(
                        noticePublisher,
                        PlatformMessage(config.language.container.endPvpTimeOut(
                            service.formatUser(session.opponent),
                            service.formatUser(session.player)
                        ))
                    )
                    is EngineGameSession -> service.buildMessage(
                        noticePublisher,
                        PlatformMessage(config.language.container.endEngineTimeOut(service.formatUser(session.humanPlayer)))
                    )
                }.launch()()

                buildFinishProcedure(
                    bot,
                    service,
                    boardPublisher,
                    config,
                    session,
                    messageBufferKey
                )()

                service.archiveSession(session, config.archivePolicy)
            }
        } else effect { }

        val report = this.writeCommandReport("expired, terminate session by ${session.gameResult!!}", channel)

        tuple(io, report)
    }

}
