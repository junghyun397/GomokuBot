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
import core.session.entities.NavigateState
import core.session.entities.NavigationKind
import kotlinx.coroutines.Deferred
import utils.assets.LinuxTime
import utils.structs.flatMap
import utils.structs.map

class SettingsCommand(override val name: String) : Command {

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
        val io = producer.produceSettings(publisher, config, 0)
            .map { it.retrieve() }
            .flatMap { settingsMessage ->
                SessionManager.addNavigate(
                    bot.sessions,
                    settingsMessage.messageRef,
                    NavigateState(NavigationKind.SETTINGS, 0, LinuxTime.withExpireOffset(bot.config.navigatorExpireOffset))
                )

                producer.attachBinaryNavigators(settingsMessage)
            }
            .map { emptyList<Order>() }

        io to this.asCommandReport("succeed", user)
    }

}
