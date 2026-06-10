package core.database.repositories

import core.assets.Channel
import core.assets.ChannelId
import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.database.jooq.tables.records.ChannelProfileRecord
import core.database.jooq.tables.references.CHANNEL_PROFILE
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Mono

object ChannelProfileRepository {

    suspend fun retrieveOrInsertChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId, produce: () -> Channel): Channel =
        this.retrieveChannel(connection, platform, givenId)
            ?: produce()
                .also { this.upsertChannel(connection, it) }

    suspend fun retrieveChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId): Channel? {
        connection.localCaches.channelProfileGivenIdCache
            .getIfPresent(givenId)
            ?.let { return it }

        val maybeChannel = this.fetchChannel(connection, platform, givenId)

        if (maybeChannel != null) {
            connection.localCaches.channelProfileGivenIdCache.put(maybeChannel.givenId, maybeChannel)
            connection.localCaches.channelProfileUidCache.put(maybeChannel.id, maybeChannel)
        }

        return maybeChannel
    }

    private suspend fun fetchChannel(connection: DatabaseConnection, channelUid: ChannelUid): Channel =
        Mono.from(
            connection.jooq
                .selectFrom(CHANNEL_PROFILE)
                .where(CHANNEL_PROFILE.CHANNEL_ID.eq(channelUid.uuid))
        )
            .map { this.extractChannel(it) }
            .awaitSingle()

    private suspend fun fetchChannel(connection: DatabaseConnection, platform: Short, givenId: ChannelId): Channel? =
        Mono.from(
            connection.jooq
                .selectFrom(CHANNEL_PROFILE)
                .where(CHANNEL_PROFILE.PLATFORM.eq(platform))
                .and(CHANNEL_PROFILE.GIVEN_ID.eq(givenId.idLong))
        )
            .map { this.extractChannel(it) }
            .awaitSingleOrNull()

    suspend fun upsertChannel(connection: DatabaseConnection, channel: Channel) {
        connection.localCaches.channelProfileGivenIdCache.put(channel.givenId, channel)
        connection.localCaches.channelProfileUidCache.put(channel.id, channel)

        Mono.from(
            connection.jooq
                .insertInto(CHANNEL_PROFILE)
                .set(CHANNEL_PROFILE.CHANNEL_ID, channel.id.uuid)
                .set(CHANNEL_PROFILE.PLATFORM, channel.platform)
                .set(CHANNEL_PROFILE.GIVEN_ID, channel.givenId.idLong)
                .set(CHANNEL_PROFILE.NAME, channel.name)
                .onConflict(CHANNEL_PROFILE.CHANNEL_ID)
                .doUpdate()
                .set(CHANNEL_PROFILE.NAME, channel.name)
        )
            .awaitSingle()
    }

    private fun extractChannel(record: ChannelProfileRecord): Channel =
        Channel(
            id = ChannelUid(record.channelId!!),
            platform = record.platform!!,
            givenId = ChannelId(record.givenId!!),
            name = record.name!!
        )

}
