package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.structs.map

class HelpCommand(override val command: String, private val sendCombined: Boolean) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        val io = run {
            if (this.sendCombined)
                buildHelpSequence(bot, config, publisher, producer, 0)
            else
                buildCombinedHelpSequence(bot, config, publisher, producer, 0)
        }
            .map { Order.Unit }

        io to this.asCommandReport("succeed", user)
    }

}
