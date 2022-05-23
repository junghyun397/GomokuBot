package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import kotlinx.coroutines.Deferred

class AcceptCommand(override val command: String, private val requestSession: RequestSession) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        val gameSession = GameManager.generatePvpSession(
            bot.config.gameExpireOffset,
            this.requestSession.owner,
            this.requestSession.opponent
        )

        SessionManager.putGameSession(bot.sessionRepository, config.id, gameSession)

        SessionManager.removeRequestSession(bot.sessionRepository, config.id, this.requestSession.owner.id)

        val io = producer.produceBeginsPVP(publisher, config.language.container, gameSession.player, gameSession.nextPlayer)
            .map { it.launch() }
            .attachBoardSequence(bot, config, producer, publisher, gameSession)
            .map { Order.DeleteSource }

        io to this.asCommandReport("accepted", user)
    }

}
