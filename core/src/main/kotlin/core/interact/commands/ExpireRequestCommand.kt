package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.RequestSession
import utils.tuple

class ExpireRequestCommand(
    private val session: RequestSession,
    private val channelAvailable: Boolean,
    private val messageAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-request"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: MessagingService,
        publisher: PublisherSet,
    ) = runCatching {
        val session = this.session
        val io = if (this.channelAvailable) {
            effect {
                val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
                val noticePublisher = publisher.plain

                if (message != null && this@ExpireRequestCommand.messageAvailable) {
                    val editPublisher = publisher.edit(message)

                    service.buildRejectedRequest(editPublisher, config.language.container, session.requester, session.recipient)
                        .launch()()
                }

                service
                    .buildRequestExpired(noticePublisher, config.language.container, session.requester, session.recipient)
                    .launch()()

                emptyOrders
            }
        } else effect { emptyOrders }

        tuple(io, this.writeCommandReport("expired, $session rejected", channel))
    }

}
