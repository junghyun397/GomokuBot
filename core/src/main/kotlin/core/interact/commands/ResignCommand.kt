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
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import kotlinx.coroutines.Deferred

class ResignCommand(override val command: String, private val session: GameSession) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        SessionManager.removeGameSession(bot.sessionRepository, config.id, session.owner.id)

        val (finishedSession, result) = GameManager.resignSession(this.session, user)

        val io = when (finishedSession) {
            is AiGameSession ->
                producer.produceSurrenderedPVE(publisher, config.language.container, finishedSession.owner)
            is PvpGameSession ->
                producer.produceSurrenderedPVP(publisher, config.language.container, result.winner, result.looser)
        }
            .map { it.launch() }
            .flatMap { producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, finishedSession) }
            .map { it.launch(); Order.BulkDelete(this.session.messageBufferKey) }

        io to this.asCommandReport("terminate session by surrendered", user)
    }

}
