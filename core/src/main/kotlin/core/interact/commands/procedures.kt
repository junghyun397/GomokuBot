package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import arrow.core.raise.ioZip
import core.BotContext
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime

fun <A, B> buildHelpProcedure(
    bot: BotContext,
    config: ChannelConfig,
    publisher: MessagePublisher<A, B>,
    service: MessagingService<A, B>,
    page: Int
): Effect<Nothing, Unit> = service.buildHelp(publisher, config.language.container, page)
    .retrieve()
    .let { io ->
        effect {
            io().fold(
                ifSome = { helpMessage ->
                    SessionManager.addNavigation(
                        bot.sessions,
                        helpMessage.messageRef,
                        PageNavigationState(
                            helpMessage.messageRef,
                            NavigationKind.ABOUT,
                            page,
                            LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                        )
                    )

                    service.attachBinaryNavigators(helpMessage.messageData)()
                },
                ifEmpty = { }
            )
        }
    }

fun <A, B> buildCombinedHelpProcedure(
    bot: BotContext,
    config: ChannelConfig,
    publisher: MessagePublisher<A, B>,
    service: MessagingService<A, B>,
    settingsPage: Int
): Effect<Nothing, Unit> = ioZip(
    service.buildHelp(publisher, config.language.container, 0).retrieve(),
    service.buildSettings(publisher, config, settingsPage).retrieve(),
)
    .let { zipped ->
        effect {
            val (maybeHelp, maybeSettings) = zipped()

            maybeHelp.fold(
                ifSome = { helpMessage ->
                    maybeSettings.fold(
                        ifSome = { settingsMessage ->
                            SessionManager.addNavigation(
                                bot.sessions,
                                helpMessage.messageRef,
                                PageNavigationState(
                                    helpMessage.messageRef,
                                    NavigationKind.ABOUT,
                                    page = 0,
                                    LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                                )
                            )

                            SessionManager.addNavigation(
                                bot.sessions,
                                settingsMessage.messageRef,
                                PageNavigationState(
                                    settingsMessage.messageRef,
                                    NavigationKind.SETTINGS,
                                    settingsPage,
                                    LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                                )
                            )

                            service.attachBinaryNavigators(helpMessage.messageData)()
                            service.attachBinaryNavigators(settingsMessage.messageData)()
                        },
                        ifEmpty = { }
                    )
                },
                ifEmpty = { }
            )
        }
    }
