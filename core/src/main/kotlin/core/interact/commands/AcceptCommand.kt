package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.structs.IO

class AcceptCommand(override val command: String, val requestSession: RequestSession) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Order>, CommandReport>> = runCatching {
        val io = producer.produceBeginsPVP(
            publisher,
            config.language.container,
            requestSession.owner,
            requestSession.opponent
        ).map { it.launch(); Order.Unit }
        io to this.asCommandReport("accepted", user)
    }

}
