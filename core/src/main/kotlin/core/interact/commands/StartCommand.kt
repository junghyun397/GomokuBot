package core.interact.commands

import core.BotContext
import core.assets.User
import core.inference.AiLevel
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
import utils.assets.LinuxTime

class StartCommand(override val command: String = "start", val opponent: User?) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        if (this.opponent != null) {
            val requestSession = RequestSession(
                user, opponent,
                SessionManager.generateMessageBufferKey(user),
                LinuxTime.withExpireOffset(bot.config.requestExpireOffset),
            )

            SessionManager.putRequestSession(bot.sessionRepository, config.id, requestSession)

            val io = producer.produceRequest(publisher, config.language.container, user, opponent)
                .map { SessionManager.appendMessage(bot.sessionRepository, requestSession.messageBufferKey, it.retrieve().message); Order.Unit }

            io to this.asCommandReport("make request to ${this.opponent}", user)
        } else {
            val gameSession = GameManager.generateAiSession(bot.config.gameExpireOffset, user, AiLevel.AMOEBA)
            SessionManager.putGameSession(bot.sessionRepository, config.id, gameSession)

            val io = producer.produceBeginsPVE(publisher, config.language.container, user, gameSession.ownerHasBlack)
                .map { it.launch() }
                .attachBoardSequence(bot, config, producer, publisher, gameSession)
                .map { Order.Unit }

            io to this.asCommandReport("start game session with AI", user)
        }
    }

}
