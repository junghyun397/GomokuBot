package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.lang.tuple
import utils.structs.flatMap
import utils.structs.map

class RejectCommand(private val requestSession: RequestSession) : Command {

    override val name = "reject"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        SessionManager.removeRequestSession(bot.sessions, guild, requestSession.owner.id)

        val editIO = service.buildRejectedRequest(publishers.edit(messageRef), config.language.container, requestSession.owner, requestSession.opponent)
            .launch()

        val noticeIO = service.buildRequestRejected(publishers.plain, config.language.container, requestSession.owner, requestSession.opponent)
            .launch()

        val io = editIO
            .flatMap { noticeIO }
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("reject ${requestSession.owner}'s request", guild, user))
    }

}
