package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
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
        producer: MessageProducer<A, B>,
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
                producer.produceSurrenderedPVE(publishers.plain, config.language.container, finishedSession.owner)
            is PvpGameSession ->
                producer.produceSurrenderedPVP(publishers.plain, config.language.container, result.winner, result.loser)
        }
            .launch()
            .flatMap { buildFinishProcedure(bot, producer, publisher, config, this.session, finishedSession) }
            .map { it + Order.ArchiveSession(finishedSession, config.archivePolicy) }

        io to this.asCommandReport("surrendered, terminate session by $result", guild, user)
    }

}
