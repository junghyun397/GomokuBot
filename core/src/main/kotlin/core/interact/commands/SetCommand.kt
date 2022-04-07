package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import jrenju.notation.Pos
import utils.structs.IO

class SetCommand(override val command: String, private val session: GameSession, private val pos: Pos) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        IO { Order.Unit } to this.asCommandReport("succeed", user)
    }

}
