package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.monads.IO

class RankCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        IO { Order.UNIT } to this.asCommandReport("succeed")
    }

}