package core.database.repositories

import core.assets.GuildUid
import core.assets.Notation
import core.assets.UserUid
import core.assets.aiUser
import core.database.DatabaseConnection
import core.database.DatabaseManager.shortAnyCastToByte
import core.database.entities.UserStats
import kotlinx.coroutines.reactive.awaitSingle
import renju.notation.Color
import renju.notation.Result
import utils.assets.LinuxTime
import utils.lang.toLinuxTime
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

                        last_update = (row["last_update"] as LocalDateTime).toLinuxTime()
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

                        last_update = (row["last_update"] as LocalDateTime).toLinuxTime()
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

                    val recordResult = Notation.ResultInstance.fromFlag(row["win_color"].shortAnyCastToByte())

                    when {
                        maybeBlackId != null -> Triple(UserUid(maybeBlackId), Notation.Color.Black, recordResult)
                        maybeWhiteId != null -> Triple(UserUid(maybeWhiteId), Notation.Color.White, recordResult)
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
                    val maybeBlackId = row["black_id"] as UUID
                    val maybeWhiteId = row["white_id"] as UUID

                    val recordResult = Notation.ResultInstance.fromFlag(row["win_color"].shortAnyCastToByte())

                    when {
                        maybeBlackId != userUid.uuid -> Triple(UserUid(maybeBlackId), Notation.Color.Black, recordResult)
                        maybeWhiteId != userUid.uuid -> Triple(UserUid(maybeWhiteId), Notation.Color.White, recordResult)
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

    private fun buildUserStats(records: List<Triple<UserUid, Color, Result>>): List<UserStats> =
        records
            .groupBy { (id, _, _) -> id }
            .map { (id, tuples) ->
                val blackTotal = tuples.count { (_, color, _) -> color == Notation.Color.Black }
                val whiteTotal = tuples.size - blackTotal

                val blackWins = tuples.count { (_, color, result) -> color == Notation.Color.Black && result.flag() == Notation.FlagInstance.BLACK() }
                val whiteWins = tuples.count { (_, color, result) -> color == Notation.Color.White && result.flag() == Notation.FlagInstance.WHITE() }

                val blackLosses = tuples.count { (_, color, result) -> color == Notation.Color.Black && result.flag() == Notation.FlagInstance.WHITE() }
                val whiteLosses = tuples.count { (_, color, result) -> color == Notation.Color.White && result.flag() == Notation.FlagInstance.BLACK() }

                UserStats(
                    userId = id,
                    blackWins = blackWins,
                    blackLosses = blackLosses,
                    blackDraws = blackTotal - blackWins - blackLosses,
                    whiteWins = whiteWins,
                    whiteLosses = whiteLosses,
                    whiteDraws = whiteTotal - whiteWins - whiteLosses,
                    last_update = LinuxTime.now()
                )
            }

}
