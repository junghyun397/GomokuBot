package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigateState
import core.session.entities.NavigationKind
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.flatMapOption
import utils.structs.map

class SettingsCommand : Command {

    override val name = "setting"

    override val responseFlag = ResponseFlag.IMMEDIATELY

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val io = producer.produceSettings(publishers.plain, config, 0)
            .retrieve()
            .flatMapOption { settingsMessage ->
                SessionManager.addNavigate(
                    bot.sessions,
                    settingsMessage.messageRef,
                    NavigateState(NavigationKind.SETTINGS, 0, LinuxTime.withOffset(bot.config.navigatorExpireOffset))
                )

                producer.attachBinaryNavigators(settingsMessage)
            }
            .map { emptyList<Order>() }

        io and this.asCommandReport("succeed", guild, user)
    }

}
