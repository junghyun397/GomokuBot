package core.interact.commands

import core.interact.message.graphics.BoardStyle
import core.interact.reports.asCommandReport
import core.BotContext
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.monads.IO
import utils.values.UserId

class StyleCommand(override val command: String, private val style: BoardStyle) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        userId: UserId,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Unit>, CommandReport>> = runCatching {
        IO { } to this.asCommandReport("${config.boardStyle.name} to ${style.name}")
    }

}
