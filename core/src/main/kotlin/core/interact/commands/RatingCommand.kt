package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageModifier
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.structs.IO

class RatingCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        modifier: MessageModifier<A, B>,
    ) = runCatching {
        IO { Order.Unit } to this.asCommandReport("succeed", user)
    }

}