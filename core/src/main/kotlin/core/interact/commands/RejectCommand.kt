package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import kotlin.time.Instant

class RejectCommand(
    private val requestSessionId: SessionId,
    private val messageRef: MessageRef,
) : Command {

    override val name = "reject"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val requestSession = SessionManager.retrieveRequestSession(bot.sessions, this.requestSessionId).snapshot()

        SessionManager.deleteRequestSession(bot.sessions, this.requestSessionId)

        val editIO = service.buildRejectedRequest(publishers.edit(this.messageRef), config.language.container, requestSession.requester, requestSession.recipient)
            .launch()

        val noticeIO = service.buildMessage(
            publishers.plain,
            PlatformMessage(config.language.container.requestRejected(
                service.formatUser(requestSession.requester),
                service.formatUser(requestSession.recipient)
            ))
        )
            .launch()

        val io = effect {
            editIO()
            noticeIO()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "reject ${requestSession.requester}", channel, user))
    }

}
