package core.database.repositories

import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.UserStats
import kotlinx.coroutines.reactive.awaitSingle
import utils.assets.LinuxTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object UserStatsRepository {

    suspend fun retrieveUserStats(connection: DatabaseConnection, userUid: UserUid): UserStats =
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

                        date = LinuxTime(row["last_update"] as Long)
                    )
                }
            }
            .defaultIfEmpty(UserStats(userUid))
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection): List<UserStats> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        SELECT 
                            *,
                            RANK () OVER (
                                ORDER BY black_wins + white_wins DESC 
                            ) wins_rank
                        FROM user_stats ORDER BY wins_rank DESC limit 10
                    """.trimIndent()
                )
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

                        date = LinuxTime((row["last_update"] as LocalDateTime).toInstant(ZoneOffset.UTC).toEpochMilli())
                    )
                }
            }
            .collectList()
            .awaitSingle()

}
