package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.interact.message.AppMessage
import core.interact.message.MessagePublisher
import core.interact.message.PlatformService
import core.session.NavigationManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.ioZip
import kotlin.time.Clock

fun buildHelpProcedure(
    bot: BotContext,
    config: ChannelConfig,
    publisher: MessagePublisher,
    service: PlatformService,
    page: Int
): Effect<Nothing, Unit> = publisher(AppMessage.Help(config.language.container, page))
    .retrieve()
    .let { io ->
        effect {
            io()?.let { helpMessage ->
                    NavigationManager.addNavigation(
                        bot.sessions,
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

fun buildCombinedHelpProcedure(
    bot: BotContext,
    config: ChannelConfig,
    publisher: MessagePublisher,
    service: PlatformService,
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
                    bot.sessions,
                    maybeHelp.ref,
                    PageNavigationState(
                        NavigationKind.ABOUT,
                        page = 0,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                NavigationManager.addNavigation(
                    bot.sessions,
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
