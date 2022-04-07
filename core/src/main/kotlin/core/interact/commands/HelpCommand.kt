package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig

class HelpCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        val io = producer.produceAboutBot(publisher, config.language.container).map { it.launch() }
            .flatMap { producer.produceCommandGuide(publisher, config.language.container) }.map { it.launch(); Order.Unit }

        io to this.asCommandReport("succeed", user)
    }

}
