package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Instant

class SettingsCommand : Command {

    override val name = "settings"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = effect {
            val message = service.buildSettings(publishers.plain, config, 0)
                .retrieve()()

            if (message != null) {
                MessageManager.addNavigation(
                    bot.sessions,
                    message.ref,
                    PageNavigationState(
                        message.ref,
                        NavigationKind.SETTINGS,
                        0,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(message)()
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
