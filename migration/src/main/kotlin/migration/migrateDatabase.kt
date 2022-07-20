package migration

import core.assets.GENERIC_PLATFORM_ID
import core.assets.Guild
import core.assets.GuildId
import core.assets.GuildUid
import core.database.DatabaseManager
import core.database.LocalCaches
import core.database.repositories.GuildProfileRepository
import java.sql.DriverManager
import java.util.*

const val DISCORD_PLATFORM_ID = 1

private val unknownGuild = Guild(
    id = GuildUid(UUID.randomUUID()),
    platform = GENERIC_PLATFORM_ID,
    givenId = GuildId(0),
    name = "unknown"
)

suspend fun main() {
    val (gomokuBotConnection, mysqlConnection) = run {
        val gomokuBotConnection = DatabaseManager.newConnectionFrom(System.getenv("GOMOKUBOT_DB_URL"), LocalCaches())
            .also { connection ->
                DatabaseManager.initDatabase(connection)
                DatabaseManager.initCaches(connection)
            }

        val mysqlConnection = DriverManager.getConnection(
            System.getenv("MYSQL_URL"),
            Properties().apply {
                put("user", System.getenv("MYSQL_USR"))
                put("password", System.getenv("MYSQL_PWD"))
            }
        )

        gomokuBotConnection to mysqlConnection
    }

    val genericGuild = GuildProfileRepository.retrieveOrInsertGuild(gomokuBotConnection, unknownGuild.platform, unknownGuild.givenId) {
        unknownGuild
    }

    migrateGuildInfoTable(gomokuBotConnection, mysqlConnection)
    migrateUserInfoTable(gomokuBotConnection, mysqlConnection)
    migrateGameRecordTable(gomokuBotConnection, mysqlConnection, genericGuild)
}
