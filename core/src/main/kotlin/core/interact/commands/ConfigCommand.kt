package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred

class ConfigCommand(
    override val command: String,
    private val newConfig: GuildConfig,
    private val configName: String,
    private val configChoice: String,
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessionRepository, config.id, newConfig)

        val io = producer.produceConfigApplied(publisher, config.language.container, this.configName, this.configChoice)
            .map { it.launch(); Order.Unit }

        io to this.asCommandReport("update $configName as $configChoice", user)
    }

}