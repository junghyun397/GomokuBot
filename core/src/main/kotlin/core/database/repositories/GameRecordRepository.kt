package core.database.repositories

import core.assets.GuildUid
import core.assets.Notation
import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.DatabaseManager.smallIntToMaybeByte
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.inference.AiLevel
import core.session.Rule
import core.session.entities.GameResult
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import renju.notation.Flag
import renju.notation.Pos
import utils.assets.LinuxTime
import utils.structs.Option
import utils.structs.asOption
import utils.structs.find
import utils.structs.map
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object GameRecordRepository {

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("CALL upload_game_record($1, $2, $3, $4, $5, $6, $7, $8, $9)")
                .bind("$1", record.boardState)
                .bind("$2", record.history.map { it.idx() }.toTypedArray())
                .bind("$3", record.gameResult.cause.id)
                .bindNullable("$4", record.gameResult.winColorId)
                .bind("$5", record.guildId.uuid)
                .bindNullable("$6", record.blackId?.uuid)
                .bindNullable("$7", record.whiteId?.uuid)
                .bindNullable("$8", record.aiLevel?.id)
                .bind("$9", record.rule.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitFirstOrNull()
    }

    suspend fun retrieveGameRecordsByGuildUid(connection: DatabaseConnection, guildUid: GuildUid, limit: Int): MutableList<GameRecord> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE guild_id = $1 ORDER BY create_date DESC LIMIT $2")
                .bind("$1", guildUid.uuid)
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
            .asOption()
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
            .asOption()
            .map { this.buildGameRecord(connection, it) }

    private fun extractGameRecordData(row: Row): GameRecordRow =
        GameRecordRow(
            (row["record_id"] as Int).toLong(),
            row["black_id"] as UUID?,
            row["white_id"] as UUID?,
            (row["board_state"] as ByteBuffer).array(),
            (row["history"] as Array<*>).map { it as Int }.toIntArray(),
            smallIntToMaybeByte(row["win_color"]),
            row["cause"] as Short,
            row["guild_id"] as UUID,
            row["ai_level"] as Short?,
            row["rule"] as Short,
            row["create_date"] as LocalDateTime
        )

    private suspend fun buildGameRecord(connection: DatabaseConnection, gameRecordRow: GameRecordRow): GameRecord {
        val blackUser = gameRecordRow.blackId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }
        val whiteUser = gameRecordRow.whiteId?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }

        return GameRecord(
            gameRecordId = Option.Some(GameRecordId(gameRecordRow.recordId)),
            boardState = gameRecordRow.boardState,
            history = gameRecordRow.history.map { Pos.fromIdx(it) },
            gameResult = GameResult.build(
                gameResult = Notation.ResultInstance.fromFlag(gameRecordRow.winColor ?: Flag.EMPTY()),
                cause = GameResult.Cause.entries.find(gameRecordRow.cause),
                blackUser = blackUser,
                whiteUser = whiteUser
            )!!,
            guildId = GuildUid(gameRecordRow.guildId),
            blackId = blackUser?.id,
            whiteId = whiteUser?.id,
            aiLevel = gameRecordRow.aiLevel?.let { AiLevel.entries.find(it) },
            rule = Rule.entries.find(gameRecordRow.rule),
            date = LinuxTime(gameRecordRow.data.toInstant(ZoneOffset.UTC).toEpochMilli()),
        )
    }

    internal class GameRecordRow(
        val recordId: Long,
        val blackId: UUID?,
        val whiteId: UUID?,
        val boardState: ByteArray,
        val history: IntArray,
        val winColor: Byte?,
        val cause: Short,
        val guildId: UUID,
        val aiLevel: Short?,
        val rule: Short,
        val data: LocalDateTime
    )

}
