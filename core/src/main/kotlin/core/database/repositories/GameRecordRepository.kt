package core.database.repositories

import core.assets.GuildUid
import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.inference.AiLevel
import core.session.GameResult
import jrenju.notation.Color
import jrenju.notation.Pos
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import utils.assets.LinuxTime
import utils.structs.find
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object GameRecordRepository {

    fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("CALL upload_game_record($1, $2, $3, $4, $5, $6, $7, $8)")
                .bind("$1", record.boardStatus)
                .bind("$2", record.history.map { it.idx() }.toTypedArray())
                .bind("$3", record.gameResult.cause.id)
                .bindNullable("$4", record.gameResult.winColorId)
                .bind("$5", record.guildId.uuid)
                .bindNullable("$6", record.blackId?.uuid)
                .bindNullable("$7", record.whiteId?.uuid)
                .bindNullable("$8", record.aiLevel?.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .subscribe()
    }

    suspend fun retrieveGameRecordsByUserUid(connection: DatabaseConnection, userUid: UserUid) =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE white_id = $1 OR black_id = $1")
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ -> row }
            }
            .flatMap { row -> mono {
                Triple(
                    row,
                    (row["black_id"] as UUID?)?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) },
                    (row["white_id"] as UUID?)?.let { UserProfileRepository.retrieveUser(connection, UserUid(it)) }
                )
            } }
            .map { (row, blackUser, whiteUser) ->
                GameRecord(
                    boardStatus = row["board_status"] as ByteArray,
                    history = (row["history"] as IntArray).map { Pos.fromIdx(it) },
                    gameResult = GameResult.build(
                        row["cause"] as Short,
                        blackUser,
                        whiteUser,
                        Color.apply((row["win_color"] as Short).toInt())
                    )!!,
                    guildId = GuildUid(row["guild_id"] as UUID),
                    blackId = blackUser?.id,
                    whiteId = whiteUser?.id,
                    aiLevel = (row["ai_level"] as Short?)?.let { AiLevel.values().find(it) } ,
                    date = LinuxTime((row["create_date"] as LocalDateTime).toInstant(ZoneOffset.UTC).toEpochMilli()),
                )
            }
            .collectList()
            .awaitSingle()

}
