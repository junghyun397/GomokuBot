package tools.migration

import core.assets.User
import core.assets.UserId
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.UserStats
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import kotlinx.coroutines.reactive.awaitLast
import reactor.kotlin.core.publisher.toFlux
import utils.lang.pair
import java.sql.Connection
import java.util.*

suspend fun migrateUserInfoTable(gomokuBotConnection: DatabaseConnection, mysqlConnection: Connection) {
    val results = mysqlConnection.createStatement()
        .executeQuery("SELECT * FROM user_info")

    val latestAnnounce = AnnounceRepository.getLatestAnnounceId(gomokuBotConnection)

    val userAndUserStats = mutableListOf<Pair<User, UserStats>>()

    while (results.next()) {
        val userId = UserId(results.getLong("user_id"))
        val name = results.getString("name_tag")
        val wins = results.getInt("win")
        val losses = results.getInt("lose")

        val blackWins = wins / 2
        val whiteWins = wins - blackWins

        val blackLosses = losses / 2
        val whiteLosses = losses  - blackLosses

        val userUid = UserUid(UUID.randomUUID())

        val user = User(
            id = userUid,
            platform = DISCORD_PLATFORM_ID,
            givenId = userId,
            name = name,
            nameTag = "unknown",
            announceId = latestAnnounce,
            profileURL = null,
        )

        val userStats = UserStats(
            userId = userUid,
            blackWins = blackWins,
            blackLosses = blackLosses,
            whiteWins = whiteWins,
            whiteLosses = whiteLosses
        )

        userAndUserStats.add(user pair userStats)
    }

    userAndUserStats.forEach { (user, _) ->
        UserProfileRepository.upsertUser(gomokuBotConnection, user)
    }

    userAndUserStats
        .toFlux()
        .doOnNext { println(it.first.name) }
        .flatMap { (_, userStats) ->
            gomokuBotConnection.liftConnection()
                .flatMapMany { dbc -> dbc
                    .createStatement(
                        """
                            INSERT INTO user_stats (user_id, black_wins, black_losses, black_draws, white_wins, white_losses, white_draws) 
                                VALUES ($1, $2, $3, $4, $5, $6, $7)
                        """.trimMargin()
                    )
                    .bind("$1", userStats.userId.uuid)
                    .bind("$2", userStats.blackWins)
                    .bind("$3", userStats.blackLosses)
                    .bind("$4", userStats.blackDraws)
                    .bind("$5", userStats.whiteWins)
                    .bind("$6", userStats.whiteLosses)
                    .bind("$7", userStats.whiteDraws)
                    .execute()
                }
                .flatMap { it.rowsUpdated }
        }
        .awaitLast()
}
