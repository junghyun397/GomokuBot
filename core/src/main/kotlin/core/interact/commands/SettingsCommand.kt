package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.tuple

class SettingsCommand : Command {

    override val name = "settings"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val io = effect {
            service.buildSettings(publishers.plain, config, 0)
                .retrieve()()
                .fold(
                    ifSome = { settingsMessage ->
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

                        service.attachBinaryNavigators(settingsMessage.messageData)()
                    },
                    ifEmpty = { }
                )

            emptyOrders
        }

        tuple(io, this.writeCommandReport("sent", guild, user))
    }

}
