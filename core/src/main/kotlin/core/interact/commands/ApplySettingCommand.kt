package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.structs.flatMap
import utils.structs.map

class ApplySettingCommand(
    override val name: String,
    private val newConfig: GuildConfig,
    private val configName: String,
    private val configChoice: String,
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, newConfig)

        val io = producer.produceConfigApplied(publisher, config.language.container, this.configName, this.configChoice)
            .flatMap { it.launch() }
            .map { emptyList<Order>() }

        io to this.asCommandReport("update $configName as $configChoice", user)
    }

}
