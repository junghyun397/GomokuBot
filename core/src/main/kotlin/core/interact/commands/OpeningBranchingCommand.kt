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
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import core.session.entities.SwapType
import utils.lang.tuple

class OpeningBranchingCommand(
    private val sessionId: SessionId,
    private val takeBranch: Boolean,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-branching"

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
        var session: BranchingStageOpeningSession? = null
        val thenSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            val branchingSession = currentSession as? BranchingStageOpeningSession ?: throw IllegalStateException()
            if (branchingSession.player.humanId != user.id) throw IllegalStateException()

            session = branchingSession
            branchingSession.branch(this.takeBranch)
        }
        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, config, service, boardPublisher, session ?: throw IllegalStateException(), thenSession)

        tuple(io, this.writeCommandReport("has chosen ${this.takeBranch}", channel, user))
    }

}
