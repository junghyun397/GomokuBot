package core.database.repositories

import core.assets.ChannelUid
import core.assets.UserUid
import core.assets.aiUser
import core.database.DatabaseConnection
import core.database.DatabaseManager.smallIntToByte
import core.database.DatabaseManager.smallIntToMaybeByte
import core.database.entities.UserStats
import kotlinx.coroutines.reactive.awaitSingle
import renju.notation.Color
import renju.notation.GameResult
import utils.assets.LinuxTime
import utils.lang.toLinuxTime
import utils.lang.tuple
import java.time.LocalDateTime
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

                        lastUpdate = (row["last_update"] as LocalDateTime).toLinuxTime()
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

                        lastUpdate = (row["last_update"] as LocalDateTime).toLinuxTime()
                    )
                }
            }
            .collectList()
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection, channelUid: ChannelUid): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM game_record WHERE guild_id = $1 AND ai_level IS NOT NULL")
                .bind("$1", channelUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    val maybeBlackId = row["black_id"] as? UUID
                    val maybeWhiteId = row["white_id"] as? UUID

                    val recordResult = GameResult.fromFlag(smallIntToMaybeByte(row["win_color"]) ?: Color.emptyFlag())

                    when {
                        maybeBlackId != null -> tuple(UserUid(maybeBlackId), Color.Black, recordResult)
                        maybeWhiteId != null -> tuple(UserUid(maybeWhiteId), Color.White, recordResult)
                        else -> throw IllegalStateException()
                    }
                }
            }
            .collectList()
            .map { records -> this.buildUserStats(records).sortedDescending() }
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection, userUid: UserUid): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "SELECT * FROM game_record WHERE ((black_id = $1 AND white_id IS NOT NULL) OR (white_id = $1 AND black_id IS NOT NULL)) AND ai_level IS NULL"
                )
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    val blackId = UserUid(row["black_id"] as UUID)
                    val whiteId = UserUid(row["white_id"] as UUID)

                    val recordResult = GameResult.fromFlag(smallIntToByte(row["win_color"]))

                    when {
                        blackId != userUid -> tuple(blackId, Color.Black, recordResult)
                        whiteId != userUid -> tuple(whiteId, Color.White, recordResult)
                        else -> throw IllegalStateException()
                    }
                }
            }
            .collectList()
            .awaitSingle()
            .let { records ->
                val userStats = this.buildUserStats(records)

                val aiStats = this.fetchUserStats(connection, userUid).reversed().copy(userId = aiUser.id)

                val unionRanking = when (aiStats.isEmpty) {
                    true -> userStats
                    else -> userStats + aiStats
                }

                unionRanking.sortedDescending()
            }

    private fun buildUserStats(records: List<Triple<UserUid, Color, GameResult>>): List<UserStats> =
        records
            .groupBy { (id, _, _) -> id }
            .map { (id, tuples) ->
                val blackTotal = tuples.count { (_, color, _) -> color == Color.Black }
                val whiteTotal = tuples.size - blackTotal

                val blackWins = tuples.count { (_, color, result) -> color == Color.Black && result.flag() == Color.Black.flag() }
                val whiteWins = tuples.count { (_, color, result) -> color == Color.White && result.flag() == Color.White.flag() }

                val blackLosses = tuples.count { (_, color, result) -> color == Color.Black && result.flag() == Color.White.flag() }
                val whiteLosses = tuples.count { (_, color, result) -> color == Color.White && result.flag() == Color.Black.flag() }

                UserStats(
                    userId = id,
                    blackWins = blackWins,
                    blackLosses = blackLosses,
                    blackDraws = blackTotal - blackWins - blackLosses,
                    whiteWins = whiteWins,
                    whiteLosses = whiteLosses,
                    whiteDraws = whiteTotal - whiteWins - whiteLosses,
                    lastUpdate = LinuxTime.now()
                )
            }

}
