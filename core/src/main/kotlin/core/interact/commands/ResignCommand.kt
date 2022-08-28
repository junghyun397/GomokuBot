package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import utils.structs.flatMap
import utils.structs.forEach
import utils.structs.map

class ResignCommand(private val session: GameSession) : Command {

    override val name = "resign"

    override val responseFlag = ResponseFlag.IMMEDIATELY

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

        SessionManager.removeGameSession(bot.sessions, guild, session.owner.id)
        finishedSession.extractGameRecord(guild.id).forEach { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }

        val io = when (finishedSession) {
            is AiGameSession ->
                producer.produceSurrenderedPVE(publishers.plain, config.language.container, finishedSession.owner)
            is PvpGameSession ->
                producer.produceSurrenderedPVP(publishers.plain, config.language.container, result.winner, result.looser)
        }
            .flatMap { it.launch() }
            .flatMap { producer.produceBoard(publishers.plain, config.language.container, config.boardStyle.renderer, finishedSession) }
            .flatMap { it.launch() }
            .map {
                listOf(
                    Order.BulkDelete(this.session.messageBufferKey),
                    Order.ArchiveSession(finishedSession, config.archivePolicy)
                )
            }

        io to this.asCommandReport("terminate session by surrendered", user)
    }

}
