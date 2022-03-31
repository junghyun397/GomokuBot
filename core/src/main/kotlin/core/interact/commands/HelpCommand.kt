package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig

class HelpCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        val io = binder.bindAboutBot(publisher, config.language.container).map { it.launch() }
            .flatMap { binder.bindCommandGuide(publisher, config.language.container) }.map { it.launch() }
            .map { Order.UNIT }

        io to this.asCommandReport("succeed")
    }

}
