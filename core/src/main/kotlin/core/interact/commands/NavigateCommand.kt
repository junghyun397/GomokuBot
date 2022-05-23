package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigateState
import core.session.entities.NavigationKind
import kotlinx.coroutines.Deferred
import utils.assets.LinuxTime
import utils.structs.IO

class NavigateCommand(
    override val command: String,
    private val navigateState: NavigateState,
    private val isForward: Boolean
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Order>, CommandReport>> = runCatching {
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

        if (this.navigateState == newState)
            return@runCatching IO { Order.Unit } to this.asCommandReport("navigate bounded", user)

        SessionManager.addNavigate(bot.sessionRepository, originalMessage.message, newState)

        val io = when (this.navigateState.navigationKind) {
            NavigationKind.ABOUT ->
                producer.paginateAboutBot(originalMessage, config.language.container, newState.page)
            NavigationKind.SETTINGS -> producer.paginateSettings(originalMessage, config, newState.page)
            else -> throw Exception()
        }
            .map { Order.Unit }

        io to this.asCommandReport("navigate ${newState.navigationKind} as ${newState.page}", user)
    }

}
