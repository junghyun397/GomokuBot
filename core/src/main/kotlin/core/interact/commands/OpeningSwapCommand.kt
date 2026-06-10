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
import core.session.entities.SessionId
import core.session.entities.SwapStageOpeningSession
import utils.lang.tuple

class OpeningSwapCommand(
    private val sessionId: SessionId,
    private val doSwap: Boolean,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet,
    ) = runCatching {
        var session: SwapStageOpeningSession? = null
        val thenSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            val swapSession = currentSession as? SwapStageOpeningSession ?: throw IllegalStateException()
            if (swapSession.player.humanId != user.id) throw IllegalStateException()

            session = swapSession
            swapSession.swap(this.doSwap)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, channel, config, service, boardPublisher, session ?: throw IllegalStateException(), thenSession)

        tuple(io, this.writeCommandReport("make swap ${this.doSwap}", channel, user))
    }

}
