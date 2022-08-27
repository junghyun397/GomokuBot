package core.interact.commands

import core.BotContext
import core.assets.Guild
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
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

class AcceptCommand(override val name: String, private val requestSession: RequestSession) : Command {

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
        val gameSession = GameManager.generatePvpSession(
            bot.config.gameExpireOffset,
            this.requestSession.owner,
            this.requestSession.opponent
        )

        SessionManager.putGameSession(bot.sessions, guild, gameSession)

        SessionManager.removeRequestSession(bot.sessions, guild, this.requestSession.owner.id)

        val io = producer.produceBeginsPVP(publisher, config.language.container, gameSession.player, gameSession.nextPlayer)
            .flatMap { it.launch() }
            .flatMap { buildBoardSequence(bot, guild, config, producer, publisher, gameSession) }
            .map { listOf(Order.DeleteSource) }

        io and this.asCommandReport("accepted", user)
    }

}
