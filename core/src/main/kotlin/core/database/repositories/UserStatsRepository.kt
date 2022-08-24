package core.database.repositories

import core.assets.GuildUid
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.UserStats
import jrenju.notation.Color
import kotlinx.coroutines.reactive.awaitSingle
import reactor.core.publisher.Flux
import scala.Enumeration
import utils.assets.LinuxTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object UserStatsRepository {

    suspend fun fetchUserStats(connection: DatabaseConnection, userUid: UserUid): UserStats =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM user_stats WHERE user_id = $1")
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    UserStats(
                        userId = userUid,
                        blackWins = row["black_wins"] as Int,
                        blackLosses = row["black_losses"] as Int,
                        blackDraws = row["black_draws"] as Int,

                        whiteWins = row["white_wins"] as Int,
                        whiteLosses = row["white_losses"] as Int,
                        whiteDraws = row["white_draws"] as Int,

                        last_update = LinuxTime(row["last_update"] as Long)
                    )
                }
            }
            .defaultIfEmpty(UserStats(userUid))
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM user_stats ORDER BY black_wins + white_wins DESC LIMIT 10")
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    UserStats(
                        userId = UserUid(row["user_id"] as UUID),
                        blackWins = row["black_wins"] as Int,
                        blackLosses = row["black_losses"] as Int,
                        blackDraws = row["black_draws"] as Int,

                        whiteWins = row["white_wins"] as Int,
                        whiteLosses = row["white_losses"] as Int,
                        whiteDraws = row["white_draws"] as Int,

                        last_update = LinuxTime((row["last_update"] as LocalDateTime).toInstant(ZoneOffset.UTC).toEpochMilli())
                    )
                }
            }
            .collectList()
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection, guildUid: GuildUid): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE guild_id = $1 AND ai_level IS NOT NULL")
                .bind("$1", guildUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    val maybeBlackId = row["black_id"] as UUID?
                    val maybeWhiteId = row["white_id"] as UUID?

                    val winColor = Color.apply((row["win_color"] as Short).toInt())

                    when {
                        maybeBlackId != null -> Triple(UserUid(maybeBlackId), Color.BLACK(), winColor)
                        maybeWhiteId != null -> Triple(UserUid(maybeWhiteId), Color.WHITE(), winColor)
                        else -> throw IllegalStateException()
                    }
                }
            }
            .awaitUserStats()

    suspend fun fetchRankings(connection: DatabaseConnection, userUid: UserUid): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "SELECT * FROM game_record WHERE ((black_id = $1 AND black_id IS NOT NULL) OR (white_id = $1 AND white_id IS NOT NULL)) AND ai_level IS NULL"
                )
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    val maybeBlackId = row["black_id"] as UUID
                    val maybeWhiteId = row["white_id"] as UUID

                    val winColor = Color.apply((row["win_color"] as Short).toInt())

                    when {
                        maybeBlackId != userUid.uuid -> Triple(UserUid(maybeBlackId), Color.BLACK(), winColor)
                        maybeWhiteId != userUid.uuid -> Triple(UserUid(maybeWhiteId), Color.WHITE(), winColor)
                        else -> throw IllegalStateException()
                    }
                }
            }
            .awaitUserStats()

    private suspend fun Flux<Triple<UserUid, Enumeration.Value, Enumeration.Value>>.awaitUserStats() = this
        .collectList()
        .map { records -> records
            .groupBy { (id, _, _) -> id }
            .map { (id, tuples) ->
                val blackTotal = tuples.count { (_, color, _) -> color == Color.BLACK() }
                val whiteTotal = tuples.size - blackTotal

                val blackWins = tuples.count { (_, color, winColor) -> color == Color.BLACK() && winColor == Color.BLACK() }
                val whiteWins = tuples.count { (_, color, winColor) -> color == Color.WHITE() && winColor == Color.WHITE() }

                val blackLosses = tuples.count { (_, color, winColor) -> color == Color.BLACK() && winColor == Color.WHITE() }
                val whiteLosses = tuples.count { (_, color, winColor) -> color == Color.WHITE() && winColor == Color.BLACK() }

                UserStats(
                    userId = id,
                    blackWins = blackWins,
                    blackLosses = blackLosses,
                    blackDraws = blackTotal - blackWins - blackLosses,
                    whiteWins = whiteWins,
                    whiteLosses = whiteLosses,
                    whiteDraws = whiteTotal - whiteWins - whiteLosses,
                    last_update = LinuxTime()
                )
            }
            .sortedDescending()
        }
        .awaitSingle()

}
