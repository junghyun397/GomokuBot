package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.ChannelSession
import core.session.entities.RequestSession
import utils.lang.tuple

class ExpireRequestCommand(
    private val channelSession: ChannelSession,
    private val session: RequestSession,
    private val channelAvailable: Boolean,
    private val messageAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-request"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        val io = if (this.channelAvailable) {
            effect {
                val message = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
                val noticePublisher = publisher.plain

                if (message != null && this@ExpireRequestCommand.messageAvailable) {
                    val editPublisher = publisher.edit(message)

                    service.buildRejectedRequest(editPublisher, config.language.container, session.owner, session.opponent)
                        .launch()()
                }

                service
                    .buildRequestExpired(noticePublisher, channelSession.config.language.container, session.owner, session.opponent)
                    .launch()()

                emptyOrders
            }
        } else effect { emptyOrders }

        tuple(io, this.writeCommandReport("expired, $session rejected", guild))
    }

}
