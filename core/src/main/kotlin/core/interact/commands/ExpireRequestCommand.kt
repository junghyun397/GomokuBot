package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.entities.ChannelConfig
import core.session.entities.RequestSession
import core.session.entities.SessionRuntime
import kotlin.time.Instant

class ExpireRequestCommand(
    private val runtime: SessionRuntime<RequestSession>,
) : InternalCommand {

    override val name = "expire-request"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        val session = this.runtime.session
        val io = if (publisher != null) {
            effect {
                buildInvalidateRequestProcedure(config, service, publisher, this@ExpireRequestCommand.runtime)()
                val noticePublisher = publisher.plain
                val notice = when (session) {
                    is RequestSession.Match -> config.language.container.requestExpired(service.formatUser(session.requester), service.formatUser(session.recipient))
                    is RequestSession.Undo -> config.language.container.undoRequestExpired(service.formatUser(session.requester), service.formatUser(session.recipient))
                }
                noticePublisher(AppMessage.Text(notice)).launch()()
            }
        } else effect { }

        CommandResult(io, this.writeActionLog(emittedTime, "$session rejected", channel))
    }

}
