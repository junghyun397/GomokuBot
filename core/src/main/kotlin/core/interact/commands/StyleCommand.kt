package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.message.graphics.BoardStyle
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.monads.IO

class StyleCommand(override val command: String, private val style: BoardStyle) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        IO { Order.UNIT } to this.asCommandReport("${config.boardStyle.name} to ${style.name}")
    }

}
