package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.entities.asGameRecord
import core.database.repositories.GameRecordRepository
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import jrenju.notation.Pos
import kotlinx.coroutines.Deferred
import utils.structs.flatMap
import utils.structs.fold
import utils.structs.map

class SetCommand(override val name: String, private val session: GameSession, private val pos: Pos) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        val thenSession = GameManager.makeMove(this.session, this.pos)

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, guild, thenSession)

                    val io = producer.produceNextMovePVP(publisher, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                        .flatMap { buildNextMoveSequence(it, bot, guild, config, producer, publisher, this.session, thenSession) }

                    io to this.asCommandReport("make move ${pos.toCartesian()}", user)
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(bot.kvineClient, thenSession, Pos.fromIdx(thenSession.board.latestMove()))

                    nextSession.gameResult.fold(
                        onEmpty = {
                            SessionManager.putGameSession(bot.sessions, guild, nextSession)

                            val io = producer.produceNextMovePVE(publisher, config.language.container, nextSession.owner, nextSession.board.latestPos().get())
                                .flatMap { buildNextMoveSequence(it, bot, guild, config, producer, publisher, this.session, nextSession) }

                            io to this.asCommandReport("make move ${pos.toCartesian()}", user)
                        },
                        onDefined = { result ->
                            SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                            GameRecordRepository.uploadGameRecord(bot.dbConnection, nextSession.asGameRecord(guild.id, result))

                            val io = when (result) {
                                is GameResult.Win ->
                                    producer.produceLosePVE(publisher, config.language.container, nextSession.owner, nextSession.board.latestPos().get())
                                is GameResult.Full ->
                                    producer.produceTiePVE(publisher, config.language.container, nextSession.owner)
                            }
                                .flatMap { it.launch() }
                                .flatMap { buildFinishSequence(bot, producer, publisher, config, this.session, nextSession) }
                                .map { it + Order.ArchiveSession(nextSession, config.archivePolicy) }

                            io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                        }
                    )
                }
            } },
            onDefined = { result -> when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                    GameRecordRepository.uploadGameRecord(bot.dbConnection, thenSession.asGameRecord(guild.id, result))

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVP(publisher, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVP(publisher, config.language.container, thenSession.owner, thenSession.opponent)
                    }
                        .flatMap { it.launch() }
                        .flatMap { buildFinishSequence(bot, producer, publisher, config, this.session, thenSession) }
                        .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                }
                is AiGameSession -> {
                    SessionManager.removeGameSession(bot.sessions, guild, this.session.owner.id)
                    GameRecordRepository.uploadGameRecord(bot.dbConnection, thenSession.asGameRecord(guild.id, result))

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVE(publisher, config.language.container, thenSession.owner, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVE(publisher, config.language.container, thenSession.owner)
                    }
                        .flatMap { it.launch() }
                        .flatMap { buildFinishSequence(bot, producer, publisher, config, this.session, thenSession) }
                        .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                }
            } }
        )
    }

}
