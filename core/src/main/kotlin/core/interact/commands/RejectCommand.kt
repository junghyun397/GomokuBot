package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
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
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        SessionManager.removeRequestSession(bot.sessions, guild, requestSession.owner.id)

        val editIO = producer.produceRequestInvalidated(publishers.edit(messageRef), config.language.container, this.requestSession.owner, this.requestSession.opponent)
            .launch()

        val noticeIO = producer.produceRequestRejected(publishers.plain, config.language.container, requestSession.owner, requestSession.opponent)
            .launch()

        val io = editIO
            .flatMap { noticeIO }
            .map { emptyList<Order>() }

        tuple(io, this.asCommandReport("reject ${requestSession.owner}'s request", guild, user))
    }

}
