package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.assets.LinuxTime
import utils.structs.Option

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

            val io = producer.produceRequest(publisher, config.language.container, user, opponent).map { it.launch(); Order.Unit }

            io to this.asCommandReport("make request to $opponent", user)
        } else {
            val gameSession = GameManager.generateSession(user, Option.Empty)
            SessionManager.putGameSession(context.sessionRepository, config.id, gameSession)

            val io = producer.produceBeginsPVE(publisher, config.language.container, user).map { it.launch(); Order.Unit }

            io to this.asCommandReport("start game session with AI", user)
        }
    }

}
