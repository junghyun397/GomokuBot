package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import arrow.core.raise.ioZip
import core.BotContext
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

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
                            Clock.System.now() + bot.config.navigatorExpireAfter.milliseconds
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
                val helpMessage = maybeHelp
                val settingsMessage = maybeSettings

                            MessageManager.addNavigation(
                                bot.sessions,
                                helpMessage.ref,
                                PageNavigationState(
                                    helpMessage.ref,
                                    NavigationKind.ABOUT,
                                    page = 0,
                                    Clock.System.now() + bot.config.navigatorExpireAfter.milliseconds
                                )
                            )

                            MessageManager.addNavigation(
                                bot.sessions,
                                settingsMessage.ref,
                                PageNavigationState(
                                    settingsMessage.ref,
                                    NavigationKind.SETTINGS,
                                    settingsPage,
                                    Clock.System.now() + bot.config.navigatorExpireAfter.milliseconds
                                )
                            )

                            service.attachBinaryNavigators(helpMessage)()
                            service.attachBinaryNavigators(settingsMessage)()
            }
        }
    }
