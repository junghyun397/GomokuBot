package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import utils.structs.IO

class ResignCommand(override val command: String, private val session: GameSession) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        SessionManager.removeGameSession(context.sessionRepository, config.id, session.owner.id)
        IO { Order.Unit } to this.asCommandReport("$session surrendered")
    }

}