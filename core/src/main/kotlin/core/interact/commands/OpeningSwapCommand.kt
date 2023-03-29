package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.GuildConfig
import core.session.entities.SwapStageOpeningSession
import utils.lang.tuple

class OpeningSwapCommand(
    private val session: SwapStageOpeningSession,
    private val doSwap: Boolean,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-swap"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val thenSession = session.swap(this.doSwap)

        SessionManager.putGameSession(bot.sessions, guild, thenSession)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, guild, config, service, boardPublisher, this.session, thenSession)

        tuple(io, this.writeCommandReport("make swap $doSwap", guild, user))
    }

}
