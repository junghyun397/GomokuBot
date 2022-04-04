package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.structs.IO
import utils.structs.Option
import utils.assets.LinuxTime

class StartCommand(override val command: String = "start", val opponent: User?) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        if (opponent != null) {
            val requestSession = RequestSession(user, opponent, LinuxTime())
            SessionManager.putRequestSession(context.sessionRepository, config.id, requestSession)

            val io = { Order.Unit }
            io to this.asCommandReport("$user request to $opponent")
        }

        val gameSession = GameManager.generateSession(user, Option.Empty)
        SessionManager.putGameSession(context.sessionRepository, config.id, gameSession)

        val io = IO { Order.Unit }
        io to this.asCommandReport("$user start game session with AI")
    }

}
