package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.GuildSession
import core.session.entities.RequestSession
import utils.lang.tuple
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.map

class ExpireRequestCommand(
    private val guildSession: GuildSession,
    private val session: RequestSession,
    private val channelAvailable: Boolean,
    private val messageAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-request"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        producer: MessageProducer<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        val io = if (this.channelAvailable) {
            val message = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val noticePublisher = publisher.plain

            val noticeIO = producer
                .produceRequestExpired(noticePublisher, guildSession.config.language.container, session.owner, session.opponent)
                .launch()

            val finishIO = if (message != null && this.messageAvailable) {
                val editPublisher = publisher.edit(message)

                producer.produceRequestInvalidated(editPublisher, config.language.container, session.owner, session.opponent)
                    .launch()
            } else IO.empty

            finishIO
                .flatMap { noticeIO }
                .map { emptyOrders }
        } else IO.value(emptyOrders)

        tuple(io, this.writeCommandReport("expired, $session rejected", guild))
    }

}
