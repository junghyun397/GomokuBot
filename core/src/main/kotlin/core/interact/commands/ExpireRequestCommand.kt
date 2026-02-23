package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.GuildSession
import core.session.entities.RequestSession
import utils.lang.tuple

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
                    .buildRequestExpired(noticePublisher, guildSession.config.language.container, session.owner, session.opponent)
                    .launch()()

                emptyOrders
            }
        } else effect { emptyOrders }

        tuple(io, this.writeCommandReport("expired, $session rejected", guild))
    }

}
