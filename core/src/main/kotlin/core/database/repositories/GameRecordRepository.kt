package core.database.repositories

import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.session.GameResult

object GameRecordRepository {

    fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("CALL upload_game_record($1, $2, $3, $4, $5, $6, $7, $8)")
                .bind("$1", record.boardStatus)
                .bindNullable("$2", record.history?.map { it.idx() }?.toTypedArray())
                .bind("$3", record.gameResult.cause.id)
                .bindNullable("$4", when (record.gameResult) {
                    is GameResult.Win -> record.gameResult.winColor.id().toShort()
                    is GameResult.Full -> null
                })
                .bind("$5", record.guildId.uuid)
                .bindNullable("$6", record.blackId?.uuid)
                .bindNullable("$7", record.whiteId?.uuid)
                .bindNullable("$8", record.aiLevel?.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .subscribe()
    }

    suspend fun retrieveGameRecordsByUserUid(connection: DatabaseConnection, userUid: UserUid): List<GameRecord> = TODO()

}
