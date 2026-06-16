package core.database.repositories

import core.assets.ChannelUid
import core.assets.User
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.database.jooq.tables.records.GameRecordRecord
import core.database.jooq.tables.references.GAME_RECORD
import core.engine.EngineLevel
import core.session.entities.Rule
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import renju.History
import renju.notation.*
import utils.find
import utils.toUtcInstant

object GameRecordRepository {

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        Mono.from(
            connection.jooq
                .insertInto(GAME_RECORD)
                .set(GAME_RECORD.HISTORY, record.history.sequence.map { it.toIdxOrNone().toShort() }.toTypedArray())
                .set(GAME_RECORD.CAUSE, record.gameResult.id)
                .set(GAME_RECORD.WIN_COLOR, record.gameResult.winner.toByte())
                .set(GAME_RECORD.CHANNEL_ID, record.channelId.uuid)
                .set(GAME_RECORD.BLACK_ID, record.users.black.id?.uuid)
                .set(GAME_RECORD.WHITE_ID, record.users.white.id?.uuid)
                .set(GAME_RECORD.ENGINE_LEVEL, record.engineLevel?.id)
                .set(GAME_RECORD.RULE, record.rule.id)
        )
            .awaitSingle()
    }

    suspend fun retrieveGameRecords(connection: DatabaseConnection, channelUid: ChannelUid, limit: Int): MutableList<GameRecord> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.CHANNEL_ID.eq(channelUid.uuid))
                .orderBy(GAME_RECORD.CREATE_DATE.desc())
                .limit(limit)
        )
            .collectList()
            .awaitSingle()
            .let { this.buildGameRecords(connection, it) }

    suspend fun retrieveGameRecords(connection: DatabaseConnection, userUid: UserUid, limit: Int): MutableList<GameRecord> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.WHITE_ID.eq(userUid.uuid).or(GAME_RECORD.BLACK_ID.eq(userUid.uuid)))
                .orderBy(GAME_RECORD.CREATE_DATE.desc())
                .limit(limit)
        )
            .collectList()
            .awaitSingle()
            .let { this.buildGameRecords(connection, it) }

    suspend fun retrieveGameRecord(connection: DatabaseConnection, recordId: GameRecordId): GameRecord? =
        Mono.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.RECORD_ID.eq(recordId.id.toInt()))
        )
            .awaitSingleOrNull()
            ?.let { this.buildGameRecord(connection, it) }

    private suspend fun buildGameRecords(connection: DatabaseConnection, records: List<GameRecordRecord>): MutableList<GameRecord> {
        val users = UserProfileRepository.retrieveUsers(connection, this.extractUserUids(records))

        return records
            .map { this.buildGameRecord(it, users) }
            .toMutableList()
    }

    private suspend fun buildGameRecord(connection: DatabaseConnection, record: GameRecordRecord): GameRecord {
        val users = UserProfileRepository.retrieveUsers(connection, this.extractUserUids(listOf(record)))

        return this.buildGameRecord(record, users)
    }

    private fun extractUserUids(records: List<GameRecordRecord>): Set<UserUid> =
        records
            .flatMap { listOfNotNull(it.blackId, it.whiteId) }
            .map { UserUid(it) }
            .toSet()

    private fun buildGameRecord(record: GameRecordRecord, users: Map<UserUid, User.Human>) =
        GameRecord(
            gameRecordId = GameRecordId(record.recordId!!.toLong()),
            history = History(record.history!!.map { Pos.fromIdxOrNone(it!!.toInt()) }),
            gameResult = GameResult.fromId(record.cause!!, Color.from(record.winColor?.toByte()))!!,
            channelId = ChannelUid(record.channelId!!),
            users = ColorContainer(
                black = record.blackId?.let { users.getValue(UserUid(it)) } ?: User.GomokuBot,
                white = record.whiteId?.let { users.getValue(UserUid(it)) } ?: User.GomokuBot
            ),
            engineLevel = record.engineLevel?.let { EngineLevel.entries.find(it) },
            rule = Rule.entries.find(record.rule!!),
            date = record.createDate!!.toUtcInstant(),
        )

}
