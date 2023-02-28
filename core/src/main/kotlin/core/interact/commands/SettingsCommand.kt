package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.flatMapOption
import utils.structs.map

class SettingsCommand : Command {

    override val name = "settings"

    override val responseFlag = ResponseFlag.Immediately

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
                SessionManager.addNavigation(
                    bot.sessions,
                    settingsMessage.messageRef,
                    PageNavigationState(
                        settingsMessage.messageRef,
                        NavigationKind.SETTINGS,
                        0,
                        LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                    )
                )

                producer.attachBinaryNavigators(settingsMessage.messageData)
            }
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("succeed", guild, user))
    }

}
