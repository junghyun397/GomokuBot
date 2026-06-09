package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.assets.humanId
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.ChannelConfig
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.GameSession
import core.session.entities.SessionId
import utils.lang.tuple

class OpeningDeclareCommand(
    private val sessionId: SessionId,
    private val maxOfferCount: Int,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        var session: DeclareStageOpeningSession? = null
        val thenSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            val declareSession = currentSession as? DeclareStageOpeningSession ?: throw IllegalStateException()
            if (declareSession.player.humanId != user.id) throw IllegalStateException()

            session = declareSession
            declareSession.declare(this.maxOfferCount)
        }
        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, channel, config, service, boardPublisher, session ?: throw IllegalStateException(), thenSession)

        tuple(io, this.writeCommandReport("declare 5th moves ${this.maxOfferCount}", channel, user))
    }

}
