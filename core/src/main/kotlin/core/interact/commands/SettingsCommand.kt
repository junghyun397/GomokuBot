package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.tuple
import kotlin.time.Clock

class SettingsCommand : Command {

    override val name = "settings"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val io = effect {
            service.buildSettings(publishers.plain, config, 0)
                .retrieve()()
                ?.let { settingsMessage ->
                        MessageManager.addNavigation(
                            bot.sessions,
                            settingsMessage.ref,
                            PageNavigationState(
                                settingsMessage.ref,
                                NavigationKind.SETTINGS,
                                0,
                                Clock.System.now() + BotConfig.navigatorExpireAfter
                            )
                        )

                        service.attachBinaryNavigators(settingsMessage)()
                    }

            emptyOrders
        }

        tuple(io, this.writeCommandReport("sent", channel, user))
    }

}
