package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.monads.IO
import utils.monads.Option
import utils.values.LinuxTime

class StartCommand(override val command: String = "start", val opponent: User?) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        if (opponent != null) {
            val requestSession = RequestSession(user, opponent, LinuxTime())
            SessionManager.putRequestSession(context.sessionRepository, config.id, requestSession)

            val io = { Order.UNIT }
            io to this.asCommandReport("$user request to $opponent")
        }

        val gameSession = GameManager.generateSession(user, Option.Empty)
        SessionManager.putGameSession(context.sessionRepository, config.id, gameSession)

        val io = IO { Order.UNIT }
        io to this.asCommandReport("$user start game session with AI")
    }

}
