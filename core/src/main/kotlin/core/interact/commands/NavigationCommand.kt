package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.IO
import utils.structs.map

class NavigationCommand(
    private val navigationState: PageNavigationState,
    private val isForward: Boolean
) : Command {

    override val name = "navigation"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val newState = this.navigationState.copy(
            page = run {
                if (this.isForward)
                    (this.navigationState.page + 1).coerceIn(this.navigationState.kind.range)
                else
                    (this.navigationState.page - 1).coerceIn(this.navigationState.kind.range)
            },
            expireDate = LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
        )

        if (this.navigationState.page == newState.page)
            return@runCatching tuple(IO.value(emptyOrders), this.writeCommandReport("navigate ${navigationState.kind} bounded", guild, user))

        SessionManager.addNavigation(bot.sessions, messageRef, newState)

        val io = when (this.navigationState.kind) {
            NavigationKind.ABOUT ->
                service.buildPaginatedHelp(publishers.edit(messageRef), config.language.container, newState.page)
            NavigationKind.SETTINGS ->
                service.buildPaginatedSettings(publishers.edit(messageRef), config, newState.page)
            else -> throw Exception()
        }
            .launch()
            .map { emptyOrders  }

        tuple(io, this.writeCommandReport("navigate ${newState.kind} as ${newState.page}", guild, user))
    }

}
