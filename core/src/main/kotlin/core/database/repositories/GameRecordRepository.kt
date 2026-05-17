package core.database.repositories

import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import core.assets.ChannelUid
import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.DatabaseManager.smallIntToMaybeByte
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.mintaka.EngineLevel
import core.session.Rule
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.Pos
import utils.lang.toUtcInstant
import utils.structs.find
import java.time.LocalDateTime
import java.util.*

object GameRecordRepository {

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                    INSERT INTO game_record (history, cause, win_color, channel_id, black_id, white_id, engine_level, rule)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                    """.trimIndent()
                )
                .bind("$1", record.history.map { it.idx }.toTypedArray())
                .bind("$2", record.gameResult.cause.id)
                .bindNullable("$3", record.gameResult.winColorId)
                .bind("$4", record.channelId.uuid)
                .bindNullable("$5", record.blackId?.uuid)
                .bindNullable("$6", record.whiteId?.uuid)
                .bindNullable("$7", record.engineLevel?.id)
                .bind("$8", record.rule.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitFirstOrNull()
    }

    suspend fun retrieveGameRecordsByChannelUid(connection: DatabaseConnection, channelUid: ChannelUid, limit: Int): MutableList<GameRecord> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE channel_id = $1 ORDER BY create_date DESC LIMIT $2")
                .bind("$1", channelUid.uuid)
                .bind("$2", limit)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ -> this.extractGameRecordData(row) }
            }
            .collectList()
            .awaitSingle()
            .map { this.buildGameRecord(connection, it) }
            .toMutableList()

    suspend fun retrieveGameRecordsByUserUid(connection: DatabaseConnection, userUid: UserUid, limit: Int): MutableList<GameRecord> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE white_id = $1 OR black_id = $1 ORDER BY create_date DESC LIMIT $2")
                .bind("$1", userUid.uuid)
                .bind("$2", limit)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ -> this.extractGameRecordData(row) }
            }
            .collectList()
            .awaitSingle()
            .map { this.buildGameRecord(connection, it) }
            .toMutableList()

    suspend fun retrieveLastGameRecordByUserUid(connection: DatabaseConnection, userUid: UserUid): Option<GameRecord> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE white_id = $1 OR black_id = $1 ORDER BY create_date DESC LIMIT 1")
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ -> this.extractGameRecordData(row) }
            }
            .awaitFirstOrNull()
            .toOption()
            .map { this.buildGameRecord(connection, it) }

    suspend fun retrieveGameRecordByRecordId(connection: DatabaseConnection, recordId: GameRecordId): Option<GameRecord> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE record_id = $1")
                .bind("$1", recordId.id)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ -> this.extractGameRecordData(row) }
            }
            .awaitFirstOrNull()
            .toOption()
            .map { this.buildGameRecord(connection, it) }

    private fun extractGameRecordData(row: Row): GameRecordRow =
        GameRecordRow(
            (row["record_id"] as Int).toLong(),
            row["black_id"] as UUID?,
            row["white_id"] as UUID?,
            (row["history"] as Array<*>).map { it as Int }.toIntArray(),
            smallIntToMaybeByte(row["win_color"]),
            row["cause"] as Short,
            row["channel_id"] as UUID,
            row["engine_level"] as Short?,
            row["rule"] as Short,
            row["create_date"] as LocalDateTime
        )

    private suspend fun buildGameRecord(connection: DatabaseConnection, gameRecordRow: GameRecordRow): GameRecord {
        val blackUser = gameRecordRow.blackId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }
        val whiteUser = gameRecordRow.whiteId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }

        return GameRecord(
            gameRecordId = Some(GameRecordId(gameRecordRow.recordId)),
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
