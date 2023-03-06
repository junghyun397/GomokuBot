package core.interact.commands

import core.BotContext
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.structs.*

fun <A, B> buildHelpProcedure(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    service: MessagingService<A, B>,
): IO<Unit> = service.buildHelp(publisher, config.language.container, 0)
    .retrieve()
    .flatMap { maybeHelpMessage ->
        maybeHelpMessage.fold(
            onDefined = { helpMessage ->
                SessionManager.addNavigation(
                    bot.sessions,
                    helpMessage.messageRef,
                    PageNavigationState(
                        helpMessage.messageRef,
                        NavigationKind.ABOUT,
                        0,
                        LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                    )
                )

                service.attachBinaryNavigators(helpMessage.messageData)
            },
            onEmpty = { IO.empty }
        )
    }

fun <A, B> buildCombinedHelpProcedure(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    service: MessagingService<A, B>,
    settingsPage: Int
): IO<Unit> = IO.zip(
    service.buildHelp(publisher, config.language.container, 0).retrieve(),
    service.buildSettings(publisher, config, settingsPage).retrieve(),
)
    .map { (maybeHelp, maybeSettings) -> Option.zip(maybeHelp, maybeSettings) }
    .flatMap { maybeTuple ->
        maybeTuple.fold(
            onDefined = { (helpMessage, settingsMessage) ->
                SessionManager.addNavigation(
                    bot.sessions,
                    helpMessage.messageRef,
                    PageNavigationState(
                        helpMessage.messageRef,
                        NavigationKind.ABOUT,
                        0,
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

                service.attachBinaryNavigators(helpMessage.messageData)
                    .flatMap { service.attachBinaryNavigators(settingsMessage.messageData) }
            },
            onEmpty = { IO.empty }
        )
    }
