package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.i18n.Language
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.announcementMessage
import core.interact.reports.writeActionLog
import core.session.NavigationManager
import core.session.SessionPool
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

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val range = this.navigationState.kind.fetchRange()

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

        NavigationManager.addNavigation(this.messageRef, newState)

        val io = effect {
            when (this@NavigationCommand.navigationState.kind) {
                NavigationKind.ABOUT ->
                    publishers.edit(this@NavigationCommand.messageRef)(AppMessage.Help(config.language.container, newState.page))
                NavigationKind.SETTINGS ->
                    publishers.edit(this@NavigationCommand.messageRef)(AppMessage.Settings(config, newState.page))
                NavigationKind.ANNOUNCE -> {
                    val announceMap = dbConnection.localCaches.announceCache[newState.page]!!

                    val content = announcementMessage(
                        config.language.container,
                        announceMap[config.language] ?: announceMap[Language.ENG]!!,
                    )
                    publishers.edit(this@NavigationCommand.messageRef)(content)
                }
                NavigationKind.BOARD -> throw Exception()
            }.launch().bind()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "navigate ${newState.kind} as ${newState.page}", channel, user))
    }

}
