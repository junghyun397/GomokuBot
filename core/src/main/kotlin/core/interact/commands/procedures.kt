package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.ioZip
import kotlin.time.Clock

fun buildHelpProcedure(
    bot: BotContext,
    config: ChannelConfig,
    publisher: MessagePublisher,
    service: MessagingService,
    page: Int
): Effect<Nothing, Unit> = service.buildHelp(publisher, config.language.container, page)
    .retrieve()
    .let { io ->
        effect {
            io()?.let { helpMessage ->
                    MessageManager.addNavigation(
                        bot.sessions,
                        helpMessage.ref,
                        PageNavigationState(
                            helpMessage.ref,
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
    service: MessagingService,
    settingsPage: Int
): Effect<Nothing, Unit> = ioZip(
    service.buildHelp(publisher, config.language.container, 0).retrieve(),
    service.buildSettings(publisher, config, settingsPage).retrieve(),
)
    .let { zipped ->
        effect {
            val (maybeHelp, maybeSettings) = zipped()

            if (maybeHelp != null && maybeSettings != null) {

                MessageManager.addNavigation(
                    bot.sessions,
                    maybeHelp.ref,
                    PageNavigationState(
                        maybeHelp.ref,
                        NavigationKind.ABOUT,
                        page = 0,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                MessageManager.addNavigation(
                    bot.sessions,
                    maybeSettings.ref,
                    PageNavigationState(
                        maybeSettings.ref,
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
