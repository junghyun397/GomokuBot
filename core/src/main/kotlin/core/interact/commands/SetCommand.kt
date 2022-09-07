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
import jrenju.notation.Pos
import utils.structs.flatMap
import utils.structs.fold
import utils.structs.forEach
import utils.structs.map

class SetCommand(
    private val session: GameSession,
    private val pos: Pos,
) : Command {

    override val name = "set"

    override val responseFlag = ResponseFlag.DEFER

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val thenSession = GameManager.makeMove(this.session, this.pos)

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, guild, thenSession)

                    val nextMoveIO = producer.produceNextMovePVP(publishers.plain, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)

                    val io = buildNextMoveProcedure(nextMoveIO, bot, guild, config, producer, publishers.plain, this.session, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}", guild, user)
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(bot.kvineClient, thenSession, Pos.fromIdx(thenSession.board.latestMove()))

                    nextSession.gameResult.fold(
                        onEmpty = {
                            SessionManager.putGameSession(bot.sessions, guild, nextSession)

                            val nextMoveIO = producer.produceNextMovePVE(publishers.plain, config.language.container, nextSession.owner, nextSession.board.latestPos().get())

                            val io = buildNextMoveProcedure(nextMoveIO, bot, guild, config, producer, publishers.plain, this.session, nextSession)

                            io to this.asCommandReport("make move ${pos.toCartesian()}", guild, user)
                        },
                        onDefined = { result ->
                            SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                            nextSession.extractGameRecord(guild.id).forEach { record ->
                                GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
                            }

                            val io = when (result) {
                                is GameResult.Win ->
                                    producer.produceLosePVE(publishers.plain, config.language.container, nextSession.owner, nextSession.board.latestPos().get())
                                is GameResult.Full ->
                                    producer.produceTiePVE(publishers.plain, config.language.container, nextSession.owner)
                            }
                                .launch()
                                .flatMap { buildFinishProcedure(bot, producer, publishers.plain, config, this.session, nextSession) }
                                .map { it + Order.ArchiveSession(nextSession, config.archivePolicy) }

                            io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", guild, user)
                        }
                    )
                }
            } },
            onDefined = { result -> when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                    thenSession.extractGameRecord(guild.id).forEach { record ->
                        GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
                    }

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVP(publishers.plain, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVP(publishers.plain, config.language.container, thenSession.owner, thenSession.opponent)
                    }
                        .launch()
                        .flatMap { buildFinishProcedure(bot, producer, publishers.plain, config, this.session, thenSession) }
                        .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", guild, user)
                }
                is AiGameSession -> {
                    SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                    thenSession.extractGameRecord(guild.id).forEach { record ->
                        GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
                    }

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVE(publishers.plain, config.language.container, thenSession.owner, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVE(publishers.plain, config.language.container, thenSession.owner)
                    }
                        .launch()
                        .flatMap { buildFinishProcedure(bot, producer, publishers.plain, config, this.session, thenSession) }
                        .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", guild, user)
                }
            } }
        )
    }

}
