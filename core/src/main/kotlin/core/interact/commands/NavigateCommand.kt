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
import core.session.entities.PageNavigateState
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.IO
import utils.structs.map

class NavigateCommand(
    private val navigateState: PageNavigateState,
    private val isForward: Boolean
) : Command {

    override val name = "navigate"

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
        val newState = this.navigateState.copy(
            page = run {
                if (this.isForward)
                    (this.navigateState.page + 1).coerceIn(this.navigateState.navigateKind.range)
                else
                    (this.navigateState.page - 1).coerceIn(this.navigateState.navigateKind.range)
            },
            expireDate = LinuxTime(this.navigateState.expireDate.timestamp + bot.config.gameExpireOffset)
        )

        if (this.navigateState.page == newState.page)
            return@runCatching IO { emptyList<Order>() } and this.asCommandReport("navigate bounded", guild, user)

        SessionManager.addNavigate(bot.sessions, messageRef, newState)

        val io = when (this.navigateState.navigateKind) {
            NavigationKind.ABOUT ->
                producer.paginateHelp(publishers.edit, config.language.container, newState.page)
            NavigationKind.SETTINGS ->
                producer.paginateSettings(publishers.edit, config, newState.page)
            else -> throw Exception()
        }
            .launch()
            .map { emptyList<Order>()  }

        io and this.asCommandReport("navigate ${newState.navigateKind} as ${newState.page}", guild, user)
    }

}
