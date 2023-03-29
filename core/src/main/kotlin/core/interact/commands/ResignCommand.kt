package core.interact.commands

import core.BotContext
import core.assets.Guild
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
import utils.structs.flatMap
import utils.structs.map

class ResignCommand(private val session: GameSession) : Command {

    override val name = "resign"

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
        val (finishedSession, result) = GameManager.resignSession(this.session, GameResult.Cause.RESIGN, user)

        GameManager.finishSession(bot, guild, finishedSession, result)

        val publisher = run {
            val boardMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            if (config.swapType == SwapType.EDIT && boardMessage != null)
                publishers.edit(boardMessage)
            else
                publishers.plain
        }

        val io = when (finishedSession) {
            is AiGameSession ->
                service.buildSurrenderedPVE(publishers.plain, config.language.container, finishedSession.owner)
            is PvpGameSession, is OpeningSession ->
                service.buildSurrenderedPVP(publishers.plain, config.language.container, result.winner, result.loser)
        }
            .launch()
            .flatMap { buildFinishProcedure(bot, service, publisher, config, this.session, finishedSession) }
            .map { it + Order.ArchiveSession(finishedSession, config.archivePolicy) }

        io to this.writeCommandReport("surrendered, terminate session by $result", guild, user)
    }

}
