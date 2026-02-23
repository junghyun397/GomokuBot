package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*

class ResignCommand(private val session: GameSession) : Command {

    override val name = "resign"

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
        val (finishedSession, result) = GameManager.resignSession(this.session, GameResult.Cause.RESIGN, user)

        GameManager.finishSession(bot, guild, finishedSession, result)

        val publisher = run {
            val boardMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            if (config.swapType == SwapType.EDIT && boardMessage != null)
                publishers.edit(boardMessage)
            else
                publishers.plain
        }

        val io = effect {
            when (finishedSession) {
                is AiGameSession ->
                    service.buildSurrenderedPVE(publishers.plain, config.language.container, finishedSession.owner)
                is PvpGameSession, is OpeningSession ->
                    service.buildSurrenderedPVP(publishers.plain, config.language.container, result.winner, result.loser)
            }.launch()()

            val finishOrders = buildFinishProcedure(bot, service, publisher, config, this@ResignCommand.session, finishedSession)()

            finishOrders + Order.ArchiveSession(finishedSession, config.archivePolicy)
        }

        io to this.writeCommandReport("surrendered, terminate session by $result", guild, user)
    }

}
