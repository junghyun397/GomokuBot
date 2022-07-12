package core.database.repositories

import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.session.GameResult
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Renju
import kotlinx.coroutines.reactive.awaitLast

object GameRecordRepository {

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        BEGIN;
                            INSERT INTO game_record (board_status, history, cause, win_color, guild_id, black_id, white_id, ai_level) 
                                VALUES ($1, $2, $3, $4, $5, $6, $7, $8);
                            UPDATE user_stats SET black_wins = black_wins + 1 WHERE user_id == $0;
                            UPDATE user_stats SET white_wins = white_wins + 1 WHERE user_id == $0;
                        COMMIT;
                    """.trimIndent()
                )
                .bind("$1", record.boardStatus)
                .bindNullable("$2", record.history?.map { it.idx() }?.toIntArray())
                .bind("$3", record.gameResult.cause.id)
                .bindNullable("$4", when (record.gameResult) {
                    is GameResult.Win -> record.gameResult.winColor.id()
                    is GameResult.Full -> null
                })
                .bind("$5", record.guildId)
                .bindNullable("$6", record.blackId)
                .bindNullable("$7", record.whiteId)
                .bindNullable("$8", record.aiLevel)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitLast()
    }

    suspend fun retrieveGameRecordsByUserUid(connection: DatabaseConnection, userUid: UserUid): List<GameRecord> = TODO()

}
