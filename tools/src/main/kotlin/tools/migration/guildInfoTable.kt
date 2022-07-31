package tools.migration

import core.assets.Guild
import core.assets.GuildId
import core.assets.GuildUid
import core.database.DatabaseConnection
import core.database.repositories.GuildConfigRepository
import core.database.repositories.GuildProfileRepository
import core.interact.i18n.Language
import core.session.entities.GuildConfig
import java.sql.Connection
import java.util.*

suspend fun migrateGuildInfoTable(gomokuBotConnection: DatabaseConnection, mysqlConnection: Connection) {
    val results = mysqlConnection.createStatement()
        .executeQuery("SELECT * FROM guild_info")

    val guildAndConfigs = mutableListOf<Pair<Guild, GuildConfig>>()

    while (results.next()) {
        val guildId = GuildId(results.getLong("guild_id"))
        val language = results.getString("lang")
            .let { if (results.wasNull()) null else it }
            ?.let { languageRaw ->
                Language.values()
                    .find { language -> language.container.languageCode().uppercase() == languageRaw.uppercase() }
            }

        val guildUid = GuildUid(UUID.randomUUID())

        val guild = Guild(
            id = guildUid,
            platform = DISCORD_PLATFORM_ID,
            givenId = guildId,
            name = "unknown"
        )

        val config = GuildConfig(
            language = language ?: Language.ENG
        )

        guildAndConfigs.add(guild to config)
    }

    guildAndConfigs.forEach { (guild, config) ->
        GuildProfileRepository.upsertGuild(gomokuBotConnection, guild)
        GuildConfigRepository.upsertGuildConfig(gomokuBotConnection, guild.id, config)
    }

}
