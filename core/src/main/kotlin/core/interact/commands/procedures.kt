package core.interact.commands

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
import kotlin.time.Clock

context(sessions: SessionPool, service: PlatformService)
fun buildHelpProcedure(
    config: ChannelConfig,
    publisher: MessagePublisher,
    page: Int
) = effect {
    val message = publisher(AppMessage.Help(config.language.container, page)).retrieve().bind()
        ?: return@effect

    NavigationManager.addNavigation(
        message.ref,
        PageNavigationState(
            NavigationKind.ABOUT,
            page,
            Clock.System.now() + BotConfig.navigatorExpireAfter
        )
    )

    service.attachBinaryNavigators(message).bind()
}

context(sessions: SessionPool, service: PlatformService)
fun buildCombinedHelpProcedure(
    config: ChannelConfig,
    publisher: MessagePublisher,
    settingsPage: Int
) = effect {
    val helpMessage = publisher(AppMessage.Help(config.language.container, 0)).retrieve().bind()
        ?: return@effect
    val settingsMessage = publisher(AppMessage.Settings(config, settingsPage)).retrieve().bind()
        ?: return@effect

    NavigationManager.addNavigation(
        helpMessage.ref,
        PageNavigationState(
            NavigationKind.ABOUT,
            page = 0,
            Clock.System.now() + BotConfig.navigatorExpireAfter
        )
    )

    NavigationManager.addNavigation(
        settingsMessage.ref,
        PageNavigationState(
            NavigationKind.SETTINGS,
            settingsPage,
            Clock.System.now() + BotConfig.navigatorExpireAfter
        )
    )

    service.attachBinaryNavigators(helpMessage).bind()
    service.attachBinaryNavigators(settingsMessage).bind()
}
