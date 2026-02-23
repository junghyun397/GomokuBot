package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.tuple

class NavigationCommand(
    private val navigationState: PageNavigationState,
    private val isForward: Boolean
) : Command {

    override val name = "navigation"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val range = this.navigationState.kind.fetchRange(bot.dbConnection)

        val newState = this.navigationState.copy(
            page = run {
                if (this.isForward)
                    (this.navigationState.page + 1).coerceIn(range)
                else
                    (this.navigationState.page - 1).coerceIn(range)
            },
            expireDate = LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
        )

        if (this.navigationState.page == newState.page)
            return@runCatching tuple(effect { emptyOrders }, this.writeCommandReport("navigate ${navigationState.kind} bounded", guild, user))

        SessionManager.addNavigation(bot.sessions, messageRef, newState)

        val io = effect {
            when (this@NavigationCommand.navigationState.kind) {
                NavigationKind.ABOUT ->
                    service.buildPaginatedHelp(publishers.edit(messageRef), config.language.container, newState.page)
                NavigationKind.SETTINGS ->
                    service.buildPaginatedSettings(publishers.edit(messageRef), config, newState.page)
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

            emptyOrders
        }

        tuple(io, this.writeCommandReport("navigate ${newState.kind} as ${newState.page}", guild, user))
    }

}
