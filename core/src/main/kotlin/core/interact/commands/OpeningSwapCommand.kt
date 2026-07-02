package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import core.session.entities.SwapStageOpeningSession
import core.session.entities.SwapType
import utils.tuple
import kotlin.time.Instant

class OpeningSwapCommand(
    private val sessionId: SessionId,
    private val doSwap: Boolean,
    private val messageRef: MessageRef
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val (session, messageBufferKey) = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            val swapSession = session as? SwapStageOpeningSession ?: throw IllegalStateException()
            if (swapSession.player.id != user.id) throw IllegalStateException()

            tuple(swapSession.swap(this.doSwap), session.messageBufferKey)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, config, service, boardPublisher, session, messageBufferKey)

        CommandResult(io, this.writeActionLog(emittedTime, "make swap ${this.doSwap}", channel, user))
    }

}
