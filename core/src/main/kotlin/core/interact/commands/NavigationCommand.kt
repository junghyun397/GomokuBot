package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.i18n.Language
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Instant

class NavigationCommand(
    private val navigationState: PageNavigationState,
    private val forward: Boolean,
    private val messageRef: MessageRef
) : Command {

    override val name = "navigation"

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
        val range = this.navigationState.kind.fetchRange(bot.dbConnection)

        val newState = this.navigationState.copy(
            page = run {
                if (this.forward)
                    (this.navigationState.page + 1).coerceIn(range)
                else
                    (this.navigationState.page - 1).coerceIn(range)
            },
            expireDate = Clock.System.now() + BotConfig.navigatorExpireAfter
        )

        if (this.navigationState.page == newState.page)
            return@runCatching CommandResult(effect { }, this.writeActionLog(emittedTime, "navigate ${navigationState.kind} bounded",
                channel, user))

        MessageManager.addNavigation(bot.sessions, this.messageRef, newState)

        val io = effect {
            when (this@NavigationCommand.navigationState.kind) {
                NavigationKind.ABOUT ->
                    service.buildPaginatedHelp(publishers.edit(this@NavigationCommand.messageRef), config.language.container, newState.page)
                NavigationKind.SETTINGS ->
                    service.buildPaginatedSettings(publishers.edit(this@NavigationCommand.messageRef), config, newState.page)
                NavigationKind.ANNOUNCE -> {
                    val announceMap = bot.dbConnection.localCaches.announceCache[newState.page]!!

                    service.buildAnnounce(
                        publishers.edit(messageRef),
                        config.language.container,
                        announceMap[config.language] ?: announceMap[Language.ENG]!!
                    )
                }
                NavigationKind.BOARD -> throw Exception()
            }.launch()()

            Unit
        }

        CommandResult(io, this.writeActionLog(emittedTime, "navigate ${newState.kind} as ${newState.page}", channel, user))
    }

}
