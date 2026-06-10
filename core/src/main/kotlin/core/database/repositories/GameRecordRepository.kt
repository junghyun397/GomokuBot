package core.database.repositories

import core.assets.ChannelUid
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.database.jooq.tables.records.GameRecordRecord
import core.database.jooq.tables.references.GAME_RECORD
import core.mintaka.EngineLevel
import core.session.Rule
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.Pos
import utils.lang.toUtcInstant
import utils.structs.find
import java.time.LocalDateTime
import java.util.*

object GameRecordRepository {

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        Mono.from(
            connection.jooq
                .insertInto(GAME_RECORD)
                .set(GAME_RECORD.HISTORY, record.history.map { it.idx as Int? }.toTypedArray())
                .set(GAME_RECORD.CAUSE, record.gameResult.cause.id)
                .set(GAME_RECORD.WIN_COLOR, record.gameResult.winColorId)
                .set(GAME_RECORD.CHANNEL_ID, record.channelId.uuid)
                .set(GAME_RECORD.BLACK_ID, record.blackId?.uuid)
                .set(GAME_RECORD.WHITE_ID, record.whiteId?.uuid)
                .set(GAME_RECORD.ENGINE_LEVEL, record.engineLevel?.id)
                .set(GAME_RECORD.RULE, record.rule.id)
        )
            .awaitSingle()
    }

    suspend fun retrieveGameRecordsByChannelUid(connection: DatabaseConnection, channelUid: ChannelUid, limit: Int): MutableList<GameRecord> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.CHANNEL_ID.eq(channelUid.uuid))
                .orderBy(GAME_RECORD.CREATE_DATE.desc())
                .limit(limit)
        )
            .map { this.extractGameRecordData(it) }
            .collectList()
            .awaitSingle()
            .map { this.buildGameRecord(connection, it) }
            .toMutableList()

    suspend fun retrieveGameRecordsByUserUid(connection: DatabaseConnection, userUid: UserUid, limit: Int): MutableList<GameRecord> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.WHITE_ID.eq(userUid.uuid).or(GAME_RECORD.BLACK_ID.eq(userUid.uuid)))
                .orderBy(GAME_RECORD.CREATE_DATE.desc())
                .limit(limit)
        )
            .map { this.extractGameRecordData(it) }
            .collectList()
            .awaitSingle()
            .map { this.buildGameRecord(connection, it) }
            .toMutableList()

    suspend fun retrieveLastGameRecordByUserUid(connection: DatabaseConnection, userUid: UserUid): GameRecord? =
        Mono.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.WHITE_ID.eq(userUid.uuid).or(GAME_RECORD.BLACK_ID.eq(userUid.uuid)))
                .orderBy(GAME_RECORD.CREATE_DATE.desc())
                .limit(1)
        )
            .map { this.extractGameRecordData(it) }
            .awaitSingleOrNull()
            ?.let { this.buildGameRecord(connection, it) }

    suspend fun retrieveGameRecordByRecordId(connection: DatabaseConnection, recordId: GameRecordId): GameRecord? =
        Mono.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.RECORD_ID.eq(recordId.id.toInt()))
        )
            .map { this.extractGameRecordData(it) }
            .awaitSingleOrNull()
            ?.let { this.buildGameRecord(connection, it) }

    private fun extractGameRecordData(record: GameRecordRecord): GameRecordRow =
        GameRecordRow(
            record.recordId!!.toLong(),
            record.blackId,
            record.whiteId,
            record.history!!.map { it!! }.toIntArray(),
            record.winColor?.toByte(),
            record.cause!!,
            record.channelId!!,
            record.engineLevel,
            record.rule!!,
            record.createDate!!
        )

    private suspend fun buildGameRecord(connection: DatabaseConnection, gameRecordRow: GameRecordRow): GameRecord {
        val blackUser = gameRecordRow.blackId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }
        val whiteUser = gameRecordRow.whiteId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }

        return GameRecord(
            gameRecordId = GameRecordId(gameRecordRow.recordId),
            history = gameRecordRow.history.map { Pos.fromIdx(it) },
            gameResult = GameResult.build(
                gameResult = GameResult.fromFlag(gameRecordRow.winColor ?: Color.emptyFlag()),
                cause = GameResult.Cause.entries.find(gameRecordRow.cause),
                blackUser = blackUser,
                whiteUser = whiteUser
            )!!,
            channelId = ChannelUid(gameRecordRow.channelId),
            blackId = blackUser?.id,
            whiteId = whiteUser?.id,
            engineLevel = gameRecordRow.engineLevel?.let { EngineLevel.entries.find(it) },
            rule = Rule.entries.find(gameRecordRow.rule),
            date = gameRecordRow.data.toUtcInstant(),
        )
    }

    internal class GameRecordRow(
        val recordId: Long,
        val blackId: UUID?,
        val whiteId: UUID?,
        val history: IntArray,
        val winColor: Byte?,
        val cause: Short,
        val channelId: UUID,
        val engineLevel: Short?,
        val rule: Short,
        val data: LocalDateTime
    )

}
