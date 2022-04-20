package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageModifier
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession

class AcceptCommand(override val command: String, private val requestSession: RequestSession) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        modifier: MessageModifier<A, B>,
    ) = runCatching {
        val gameSession = GameManager.generatePvpSession(requestSession.owner, requestSession.opponent)

        SessionManager.putGameSession(context.sessionRepository, config.id, gameSession)

        SessionManager.removeRequestSession(context.sessionRepository, config.id, requestSession.owner.id)

        val io = producer.produceBeginsPVP(publisher, config.language.container, requestSession.owner, requestSession.opponent)
            .map { it.launch() }
            .flatMap { producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, gameSession) }
            .map { producer.attachFocusButtons(it, config.language.container, config.focusPolicy, gameSession) }
            .map { SessionManager.appendMessage(context.sessionRepository, gameSession.messageBufferKey, it.retrieve()); Order.DeleteSource }

        io to this.asCommandReport("accepted", user)
    }

}
