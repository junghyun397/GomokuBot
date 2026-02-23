package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.ChannelConfig
import utils.lang.tuple

class OpeningBranchingCommand(
    private val session: BranchingStageOpeningSession,
    private val takeBranch: Boolean,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-branching"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val thenSession = session.branch(this.takeBranch)

        SessionManager.putGameSession(bot.sessions, guild, thenSession)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, guild, config, service, boardPublisher, this.session, thenSession)

        tuple(io, this.writeCommandReport("has chosen $takeBranch", guild, user))
    }

}
