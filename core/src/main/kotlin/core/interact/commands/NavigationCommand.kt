package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.and
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
        producer: MessageProducer<A, B>,
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
            return@runCatching IO { emptyList<Order>() } and this.asCommandReport("navigate ${navigationState.kind} bounded", guild, user)

        SessionManager.addNavigation(bot.sessions, messageRef, newState)

        val io = when (this.navigationState.kind) {
            NavigationKind.ABOUT ->
                producer.paginateHelp(publishers.edit, config.language.container, newState.page)
            NavigationKind.SETTINGS ->
                producer.paginateSettings(publishers.edit, config, newState.page)
            else -> throw Exception()
        }
            .launch()
            .map { emptyList<Order>()  }

        io and this.asCommandReport("navigate ${newState.kind} as ${newState.page}", guild, user)
    }

}
