package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.*
import kotlin.time.Instant

class OpeningBranchingCommand(
    private val sessionId: SessionId,
    private val takeBranch: Boolean,
    private val messageRef: MessageRef,
) : Command {

    override val name = "opening-branching"

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
        var messageBufferKey: MessageBufferKey? = null

        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            val branchingSession = session as? BranchingStageOpeningSession ?: throw IllegalStateException()
            if (branchingSession.player.id != user.id) throw IllegalStateException()

            messageBufferKey = session.messageBufferKey
            branchingSession.branch(this.takeBranch)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, config, service, boardPublisher, session, messageBufferKey!!)

        CommandResult(io, this.writeActionLog(emittedTime, "has chosen ${this.takeBranch}", channel, user))
    }

}
