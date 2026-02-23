package core.database.repositories

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.Channel
import core.assets.ChannelId
import core.assets.ChannelUid
import core.database.DatabaseConnection
import kotlinx.coroutines.reactive.awaitSingle
import java.util.*

object ChannelProfileRepository {

    suspend fun retrieveOrInsertChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId, produce: () -> Channel): Channel =
        when (val maybeChannel = this.retrieveChannel(connection, platform, givenId)) {
            is Some -> maybeChannel.value
            None -> produce()
                .also { this.upsertChannel(connection, it) }
        }

    suspend fun retrieveChannel(connection: DatabaseConnection, channelUid: ChannelUid): Channel =
        connection.localCaches.channelProfileUidCache
            .getIfPresent(channelUid)
            ?: this.fetchChannel(connection, channelUid)
                .also { guild ->
                    connection.localCaches.channelProfileGivenIdCache.put(guild.givenId, guild)
                    connection.localCaches.channelProfileUidCache.put(guild.id, guild)
                }

    suspend fun retrieveChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId): Option<Channel> {
        connection.localCaches.channelProfileGivenIdCache
            .getIfPresent(givenId)
            ?.let { return Some(it) }

        val maybeChannel = this.fetchChannel(connection, platform, givenId)

        if (maybeChannel is Some) {
            connection.localCaches.channelProfileGivenIdCache.put(maybeChannel.value.givenId, maybeChannel.value)
            connection.localCaches.channelProfileUidCache.put(maybeChannel.value.id, maybeChannel.value)
        }

        return maybeChannel
    }

    private suspend fun fetchChannel(connection: DatabaseConnection, channelUid: ChannelUid): Channel =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM guild_profile WHERE guild_id = $1")
                .bind("$1", channelUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Channel(
                        id = ChannelUid(row["guild_id"] as UUID),
                        platform = row["platform"] as Short,
                        givenId = ChannelId(row["given_id"] as Long),
                        name = row["name"] as String
                    )
                }
            }
            .awaitSingle()

    private suspend fun fetchChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId): Option<Channel> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM guild_profile WHERE platform = $1 AND given_id = $2")
                .bind("$1", platform)
                .bind("$2", givenId.idLong)
                .execute()
            }
            .flatMap<Option<Channel>> { result -> result
                .map { row, _ ->
                    Some(Channel(
                        id = ChannelUid(row["guild_id"] as UUID),
                        platform = platform,
                        givenId = givenId,
                        name = row["name"] as String
                    ))
                }
            }
            .defaultIfEmpty(None)
            .awaitSingle()

    suspend fun upsertChannel(connection: DatabaseConnection, guild: Channel) {
        connection.localCaches.channelProfileGivenIdCache.put(guild.givenId, guild)
        connection.localCaches.channelProfileUidCache.put(guild.id, guild)

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
