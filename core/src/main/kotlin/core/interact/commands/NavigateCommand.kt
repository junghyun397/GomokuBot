package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigateState
import core.session.entities.NavigationKind
import kotlinx.coroutines.Deferred
import utils.assets.LinuxTime
import utils.structs.IO
import utils.structs.map

class NavigateCommand(
    override val command: String,
    private val navigateState: NavigateState,
    private val isForward: Boolean
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>
    ) = runCatching {
        val originalMessage = message.await()

        val newState = this.navigateState.copy(
            page = run {
                if (this.isForward)
                    (this.navigateState.page + 1).coerceIn(this.navigateState.navigationKind.range)
                else
                    (this.navigateState.page - 1).coerceIn(this.navigateState.navigationKind.range)
            },
            expireDate = LinuxTime(this.navigateState.expireDate.timestamp + bot.config.gameExpireOffset)
        )

        if (this.navigateState.page == newState.page)
            return@runCatching IO { emptyList<Order>() } to this.asCommandReport("navigate bounded", user)

        SessionManager.addNavigate(bot.sessions, originalMessage.messageRef, newState)

        val io = when (this.navigateState.navigationKind) {
            NavigationKind.ABOUT ->
                producer.paginateHelp(editPublisher, config.language.container, newState.page)
            NavigationKind.SETTINGS ->
                producer.paginateSettings(editPublisher, config, newState.page)
            else -> throw Exception()
        }
            .map { it.launch(); emptyList<Order>() }

        io to this.asCommandReport("navigate ${newState.navigationKind} as ${newState.page}", user)
    }

}
