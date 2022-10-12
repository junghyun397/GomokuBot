package tools.migration

import core.assets.Guild
import core.assets.Notation
import core.assets.UserId
import core.assets.bindNullable
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.repositories.UserProfileRepository
import core.inference.AiLevel
import core.session.GameResult
import kotlinx.coroutines.reactive.awaitLast
import reactor.kotlin.core.publisher.toFlux
import renju.Board
import renju.notation.Flag
import renju.notation.Pos
import renju.notation.Result
import utils.assets.LinuxTime
import utils.lang.pair
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

        val board = history.fold<Pos, Board>(Notation.EmptyBoard) { board, pos -> board.makeMove(pos) }

        val winColor = when {
            board.moves() == 0 -> Notation.Color.Black.flag()
            else -> when (cause) {
                GameResult.Cause.FIVE_IN_A_ROW -> {
                    if (board.winner().isEmpty)
                        continue
                    else
                        (board.winner().get() as Result.FiveInRow).winner().flag()
                }
                GameResult.Cause.RESIGN, GameResult.Cause.TIMEOUT -> board.color().flag()
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
                Flag.BLACK() -> user pair null
                else -> null pair user
            }
            "LOSE", "RESIGN", "TIMEOUT" -> when (winColor) {
                Flag.WHITE() -> null pair user
                else -> user pair null
            }
            "FULL" -> when (board.color()) {
                Notation.Color.Black -> null pair user
                else -> user pair null
            }
            else -> throw IllegalStateException()
        }

        val gameResult = GameResult.build(
            gameResult = Notation.ResultInstance.fromFlag(winColor!!),
            cause = cause,
            blackUser = blackUser,
            whiteUser = whiteUser
        )!!

        val record = GameRecord(
            boardStatus = board.field(),
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
