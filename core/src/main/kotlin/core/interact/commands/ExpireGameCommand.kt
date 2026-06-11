package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.MessageManager
import core.session.entities.*
import renju.notation.GameResult
import utils.lang.replaceIf
import utils.lang.tuple

class ExpireGameCommand(
    private val session: GameSession,
    private val channelAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: MessagingService,
        publisher: PublisherSet,
    ) = runCatching {
        val session = this.session
        val (finishedSession, result) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

        GameManager.finishSession(bot, channel, finishedSession)

        val io = if (this.channelAvailable) {
            effect {
                val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

                val noticePublisher = publisher.plain

                val boardPublisher = noticePublisher
                    .replaceIf(config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

                when (session) {
                    is PvpGameSession, is OpeningSession -> service
                        .buildTimeoutPvp(noticePublisher, config.language.container, session.opponent, session.player)
                    is EngineGameSession -> service
                        .buildTimeoutEngine(noticePublisher, config.language.container, session.humanPlayer)
                }.launch()()

                val finishOrders = buildFinishProcedure(
                    bot,
                    service,
                    boardPublisher,
                    config,
                    session,
                    finishedSession
                )()

                finishOrders + Order.ArchiveSession(finishedSession, config.archivePolicy)
            }
        } else effect { emptyOrders }

        val report = this.writeCommandReport("expired, terminate session by $result", channel)

        tuple(io, report)
    }

}
