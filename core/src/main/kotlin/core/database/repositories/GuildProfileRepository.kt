package core.database.repositories

import core.assets.Guild
import core.assets.GuildId
import core.assets.GuildUid
import core.database.DatabaseConnection
import kotlinx.coroutines.reactive.awaitSingle
import utils.structs.*
import java.util.*

object GuildProfileRepository {

    suspend fun retrieveOrInsertGuild(connection: DatabaseConnection, platform: Short, givenId: GuildId, produce: () -> Guild): Guild =
        this.retrieveGuild(connection, platform, givenId)
            .orElseGet {
                produce()
                    .also { this.upsertGuild(connection, it) }
            }

    suspend fun retrieveGuild(connection: DatabaseConnection, guildUid: GuildUid): Guild =
        connection.localCaches.guildProfileUidCache
            .getIfPresent(guildUid)
            .asOption()
            .orElseGet {
                this.fetchGuild(connection, guildUid)
                    .also { guild ->
                        connection.localCaches.guildProfileGivenIdCache.put(guild.givenId, guild)
                        connection.localCaches.guildProfileUidCache.put(guild.id, guild)
                    }
            }

    suspend fun retrieveGuild(connection: DatabaseConnection, platform: Short, givenId: GuildId): Option<Guild> =
        connection.localCaches.guildProfileGivenIdCache
            .getIfPresent(givenId)
            .asOption()
            .orElse {
                this.fetchGuild(connection, platform, givenId)
                    .also { maybeGuild -> maybeGuild
                        .forEach {guild ->
                            connection.localCaches.guildProfileGivenIdCache.put(guild.givenId, guild)
                            connection.localCaches.guildProfileUidCache.put(guild.id, guild)
                        }
                    }
            }

    private suspend fun fetchGuild(connection: DatabaseConnection, guildUid: GuildUid): Guild =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM guild_profile WHERE guild_id = $1")
                .bind("$1", guildUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Guild(
                        id = GuildUid(row["guild_id"] as UUID),
                        platform = row["platform"] as Short,
                        givenId = GuildId(row["given_id"] as Long),
                        name = row["name"] as String
                    )
                }
            }
            .awaitSingle()

    private suspend fun fetchGuild(connection: DatabaseConnection, platform: Short, givenId: GuildId): Option<Guild> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM guild_profile WHERE platform = $1 AND given_id = $2")
                .bind("$1", platform)
                .bind("$2", givenId.idLong)
                .execute()
            }
            .flatMap<Option<Guild>> { result -> result
                .map { row, _ ->
                    Option.Some(Guild(
                        id = GuildUid(row["guild_id"] as UUID),
                        platform = platform,
                        givenId = givenId,
                        name = row["name"] as String
                    ))
                }
            }
            .defaultIfEmpty(Option.Empty)
            .awaitSingle()

    suspend fun upsertGuild(connection: DatabaseConnection, guild: Guild) {
        connection.localCaches.guildProfileGivenIdCache.put(guild.givenId, guild)
        connection.localCaches.guildProfileUidCache.put(guild.id, guild)

        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        INSERT INTO guild_profile (guild_id, platform, given_id, name) VALUES ($1, $2, $3, $4)
                            ON CONFLICT (guild_id) DO UPDATE SET name = $4
                    """.trimIndent()
                )
                .bind("$1", guild.id.uuid)
                .bind("$2", guild.platform)
                .bind("$3", guild.givenId.idLong)
                .bind("$4", guild.name)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitSingle()
    }

}
