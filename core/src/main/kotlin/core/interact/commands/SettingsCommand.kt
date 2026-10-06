package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.NavigationManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Instant

class SettingsCommand : Command {

    override val name = "settings"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = effect {
            val message = publishers.plain(AppMessage.Settings(config, 0))
                .retrieve().bind()

            if (message != null) {
                NavigationManager.addNavigation(
                    message.ref,
                    PageNavigationState(
                        NavigationKind.SETTINGS,
                        0,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(message).bind()
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
