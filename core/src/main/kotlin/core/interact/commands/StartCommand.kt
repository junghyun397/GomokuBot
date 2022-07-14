package core.interact.commands

import core.BotContext
import core.assets.Guild
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
import utils.structs.flatMap
import utils.structs.map

class StartCommand(override val command: String = "start", val opponent: User?) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        when(this.opponent) {
            null -> {
                val gameSession = GameManager.generateAiSession(bot.config.gameExpireOffset, user, AiLevel.AMOEBA)
                SessionManager.putGameSession(bot.sessions, guild, gameSession)

                val io = producer.produceBeginsPVE(publisher, config.language.container, user, gameSession.ownerHasBlack)
                    .map { it.launch() }
                    .flatMap { buildBoardSequence(bot, guild, config, producer, publisher, gameSession) }
                    .map { emptyList<Order>() }

                io to this.asCommandReport("start game session with AI", user)
            }
            else -> {
                val requestSession = RequestSession(
                    user, opponent,
                    SessionManager.generateMessageBufferKey(user),
                    LinuxTime.withExpireOffset(bot.config.requestExpireOffset),
                )

                SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                val io = producer.produceRequest(publisher, config.language.container, user, opponent)
                    .map { SessionManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.retrieve().messageRef); emptyList<Order>() }

                io to this.asCommandReport("make request to ${this.opponent}", user)
            }
        }
    }

}
