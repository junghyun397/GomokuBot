package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotConfig
import core.interact.message.AppMessage
import core.interact.message.MessagePublisher
import core.interact.message.PlatformService
import core.session.NavigationManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.ioZip
import kotlin.time.Clock

context(sessions: SessionPool, service: PlatformService)
fun buildHelpProcedure(
    config: ChannelConfig,
    publisher: MessagePublisher,
    page: Int
): Effect<Nothing, Unit> = publisher(AppMessage.Help(config.language.container, page))
    .retrieve()
    .let { io ->
        effect {
            io()?.let { helpMessage ->
                NavigationManager.addNavigation(
                    helpMessage.ref,
                    PageNavigationState(
                        NavigationKind.ABOUT,
                        page,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(helpMessage)()
            }
        }
    }

context(sessions: SessionPool, service: PlatformService)
fun buildCombinedHelpProcedure(
    config: ChannelConfig,
    publisher: MessagePublisher,
    settingsPage: Int
): Effect<Nothing, Unit> = ioZip(
    publisher(AppMessage.Help(config.language.container, 0)).retrieve(),
    publisher(AppMessage.Settings(config, settingsPage)).retrieve(),
)
    .let { zipped ->
        effect {
            val (maybeHelp, maybeSettings) = zipped()

            if (maybeHelp != null && maybeSettings != null) {

                NavigationManager.addNavigation(
                    maybeHelp.ref,
                    PageNavigationState(
                        NavigationKind.ABOUT,
                        page = 0,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                NavigationManager.addNavigation(
                    maybeSettings.ref,
                    PageNavigationState(
                        NavigationKind.SETTINGS,
                        settingsPage,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(maybeHelp)()
                service.attachBinaryNavigators(maybeSettings)()
            }
        }
    }
