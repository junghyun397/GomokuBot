package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import kotlinx.coroutines.Deferred
import utils.structs.map

class RejectCommand(override val command: String, private val requestSession: RequestSession) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        SessionManager.removeRequestSession(bot.sessions, guild, requestSession.owner.id)

        val io = producer.produceRequestRejected(publisher, config.language.container, requestSession.owner, requestSession.opponent)
            .map { it.launch(); listOf(Order.DeleteSource) }

        io to this.asCommandReport("reject ${requestSession.owner}'s request", user)
    }

}