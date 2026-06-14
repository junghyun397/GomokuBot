package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.*
import utils.tuple

class OpeningDeclareCommand(
    private val sessionId: SessionId,
    private val maxOfferCount: Int,
    private val messageRef: MessageRef
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        var messageBufferKey: MessageBufferKey? = null

        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            val declareSession = session as? DeclareStageOpeningSession ?: throw IllegalStateException()
            if (declareSession.player.id != user.id) throw IllegalStateException()

            messageBufferKey = session.messageBufferKey
            declareSession.declare(this.maxOfferCount)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, config, service, boardPublisher, session, messageBufferKey!!)

        tuple(io, this.writeCommandReport("declare 5th moves ${this.maxOfferCount}", channel, user))
    }

}
