package migration

import core.assets.Guild
import core.assets.UserId
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.repositories.UserProfileRepository
import core.inference.AiLevel
import core.session.GameResult
import jrenju.Board
import jrenju.`EmptyBoard$`
import jrenju.notation.Color
import jrenju.notation.Pos
import kotlinx.coroutines.reactive.awaitLast
import reactor.kotlin.core.publisher.toFlux
import utils.assets.LinuxTime
import utils.structs.getOrException
import java.sql.Connection

suspend fun migrateGameRecordTable(gomokuBotConnection: DatabaseConnection, mysqlConnection: Connection, genericGuild: Guild) {
    val results = mysqlConnection.createStatement()
        .executeQuery("SELECT * FROM game_record WHERE reason != 'PVPWIN' ORDER BY record_index ASC")

    val gameRecords = mutableListOf<GameRecord>()

    while (results.next()) {
        val userId = UserId(results.getLong("user_id"))
        val date = LinuxTime(results.getLong("date"))

        val cause = when (results.getString("reason")) {
            "WIN", "LOSE" -> GameResult.Cause.FIVE_IN_A_ROW
            "RESIGN" -> GameResult.Cause.RESIGN
            "TIMEOUT" -> GameResult.Cause.TIMEOUT
            "FULL" -> GameResult.Cause.DRAW
            else -> throw IllegalStateException()
        }

        val history = results.getString("record_data").split(":")
            .drop(1)
            .dropLast(1)
            .map { it.split(".") }
            .map { (row, col) -> Pos.rowColToIdx(row.toInt(), col.toInt()) }
            .map { Pos.fromIdx(it) }

        val board = history.fold(`EmptyBoard$`.`MODULE$` as Board) { board, pos -> board.makeMove(pos) }

        val winColor = when {
            board.moves() == 0 -> Color.BLACK()
            else -> when (cause) {
                GameResult.Cause.FIVE_IN_A_ROW -> {
                    if (board.winner().isEmpty)
                        continue
                    else
                        Color.apply((board.winner().get() as Byte).toInt())
                }
                GameResult.Cause.RESIGN, GameResult.Cause.TIMEOUT -> board.color()
                GameResult.Cause.DRAW -> null
            }
        }

        val userRaw = UserProfileRepository.retrieveUser(gomokuBotConnection, DISCORD_PLATFORM_ID, userId)

        val user = if (userRaw.isEmpty)
            continue
        else
            userRaw.getOrException()

        val (blackUser, whiteUser) = when (results.getString("reason")) {
            "WIN" -> when (winColor) {
                Color.BLACK() -> user to null
                else -> null to user
            }
            "LOSE", "RESIGN", "TIMEOUT" -> when (winColor) {
                Color.BLACK() -> null to user
                else -> user to null
            }
            "FULL" -> when (board.color()) {
                Color.BLACK() -> null to user
                else -> user to null
            }
            else -> throw IllegalStateException()
        }

        val gameResult = GameResult.build(
            cause = cause,
            blackUser = blackUser,
            whiteUser = whiteUser,
            winColor = winColor
        )!!

        val record = GameRecord(
            boardStatus = board.boardField(),
            history = history,
            gameResult = gameResult,
            guildId = genericGuild.id,
            blackId = blackUser?.id,
            whiteId = whiteUser?.id,
            aiLevel = AiLevel.APE,
            date = date
        )

        gameRecords.add(record)
    }

    gameRecords
        .toFlux()
        .flatMap { record ->
            gomokuBotConnection.liftConnection()
                .flatMapMany { dbc -> dbc
                    .createStatement(
                        """
                            INSERT INTO game_record (board_status, history, cause, win_color, guild_id, black_id, white_id, ai_level)
                                VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                        """.trimMargin()
                    )
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
        }
        .awaitLast()
}
