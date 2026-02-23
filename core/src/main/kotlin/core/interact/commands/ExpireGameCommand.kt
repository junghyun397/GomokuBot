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
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import renju.notation.GameResult
import utils.lang.replaceIf
import utils.lang.tuple

class ExpireGameCommand(
    private val channelSession: ChannelSession,
    private val session: GameSession,
    private val channelAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        val (finishedSession, result) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

        GameManager.finishSession(bot, channelSession.guild, finishedSession, result)

        val io = if (this.channelAvailable) {
            effect {
                val message = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

                val noticePublisher = publisher.plain

                val boardPublisher = noticePublisher
                    .replaceIf(channelSession.config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

                when (session) {
                    is PvpGameSession, is OpeningSession -> service
                        .buildTimeoutPVP(noticePublisher, channelSession.config.language.container, session.nextPlayer, session.player)
                    is AiGameSession -> service
                        .buildTimeoutPVE(noticePublisher, channelSession.config.language.container, session.owner)
                }.launch()()

                val finishOrders = buildFinishProcedure(
                    bot,
                    service,
                    boardPublisher,
                    channelSession.config,
                    session,
                    finishedSession
                )()

                finishOrders + Order.ArchiveSession(finishedSession, channelSession.config.archivePolicy)
            }
        } else effect { emptyOrders }

        val report = this.writeCommandReport("expired, terminate session by $result", guild)

        tuple(io, report)
    }

}
