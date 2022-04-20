package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageModifier
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession

class RejectCommand(override val command: String, private val requestSession: RequestSession) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        modifier: MessageModifier<A, B>,
    ) = runCatching {
        SessionManager.removeRequestSession(context.sessionRepository, config.id, requestSession.owner.id)

        val io = producer.produceRequestRejected(publisher, config.language.container, requestSession.owner, requestSession.opponent)
            .map { it.launch(); Order.DeleteSource }

        io to this.asCommandReport("reject ${requestSession.owner}'s request", user)
    }

}