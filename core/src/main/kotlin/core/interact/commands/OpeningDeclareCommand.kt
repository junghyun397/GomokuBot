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
import core.session.entities.ChannelConfig
import core.session.entities.DeclareStageOpeningSession
import utils.lang.tuple

class OpeningDeclareCommand(
    private val session: DeclareStageOpeningSession,
    private val maxOfferCount: Int,
    private val deployAt: MessageRef?
) : Command {

    override val name = "opening-swap"

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
        val thenSession = session.declare(this.maxOfferCount)

        SessionManager.putGameSession(bot.sessions, guild, thenSession)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val io = buildNextMoveProcedure(bot, guild, config, service, boardPublisher, this.session, thenSession)

        tuple(io, this.writeCommandReport("declare 5th moves $maxOfferCount", guild, user))
    }

}
