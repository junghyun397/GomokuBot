package core.database.repositories

import core.assets.User
import core.assets.UserId
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.jooq.tables.records.UserProfileRecord
import core.database.jooq.tables.references.USER_PROFILE
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Mono

object UserProfileRepository {

    suspend fun retrieveOrInsertUser(connection: DatabaseConnection, platform: Short, givenId: UserId, produce: () -> User.Human): User.Human =
        this.retrieveUser(connection, platform, givenId)
            ?: produce()
                .copy(announceId = AnnounceRepository.getLatestAnnounceId(connection))
                .also { this.upsertUser(connection, it) }

    suspend fun retrieveUser(connection: DatabaseConnection, userUid: UserUid): User.Human =
        connection.localCaches.userProfileUidCache
            .getIfPresent(userUid)
            ?: this.fetchUser(connection, userUid)
                .also { user ->
                    connection.localCaches.userProfileUidCache.put(user.id, user)
                    connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
                }

    suspend fun retrieveUser(connection: DatabaseConnection, platform: Short, givenId: UserId): User.Human? {
        connection.localCaches.userProfileGivenIdCache
            .getIfPresent(givenId)
            ?.let { return it }

        val maybeUser = this.fetchUser(connection, platform, givenId)

        if (maybeUser != null) {
            connection.localCaches.userProfileUidCache.put(maybeUser.id, maybeUser)
            connection.localCaches.userProfileGivenIdCache.put(maybeUser.givenId, maybeUser)
        }

        return maybeUser
    }

    private suspend fun fetchUser(connection: DatabaseConnection, userUid: UserUid): User.Human =
        Mono.from(
            connection.jooq
                .selectFrom(USER_PROFILE)
                .where(USER_PROFILE.USER_ID.eq(userUid.uuid))
        )
            .map { this.extractUser(it) }
            .awaitSingle()

    private suspend fun fetchUser(connection: DatabaseConnection, platform: Short, givenId: UserId): User.Human? =
        Mono.from(
            connection.jooq
                .selectFrom(USER_PROFILE)
                .where(USER_PROFILE.PLATFORM.eq(platform))
                .and(USER_PROFILE.GIVEN_ID.eq(givenId.idLong))
        )
            .map { this.extractUser(it) }
            .awaitSingleOrNull()

    suspend fun upsertUser(connection: DatabaseConnection, user: User.Human) {
        connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
        connection.localCaches.userProfileUidCache.put(user.id, user)

        Mono.from(
            connection.jooq
                .insertInto(USER_PROFILE)
                .set(USER_PROFILE.USER_ID, user.id.uuid)
                .set(USER_PROFILE.PLATFORM, user.platform)
                .set(USER_PROFILE.GIVEN_ID, user.givenId.idLong)
                .set(USER_PROFILE.NAME, user.name)
                .set(USER_PROFILE.UNIQUE_NAME, user.uniqueName)
                .set(USER_PROFILE.ANNOUNCE_ID, user.announceId)
                .set(USER_PROFILE.PROFILE_URL, user.profileURL)
                .onConflict(USER_PROFILE.USER_ID)
                .doUpdate()
                .set(USER_PROFILE.PLATFORM, user.platform)
                .set(USER_PROFILE.NAME, user.name)
                .set(USER_PROFILE.UNIQUE_NAME, user.uniqueName)
                .set(USER_PROFILE.ANNOUNCE_ID, user.announceId)
                .set(USER_PROFILE.PROFILE_URL, user.profileURL)
        )
            .awaitSingle()
    }

    private fun extractUser(record: UserProfileRecord): User.Human =
        User.Human(
            id = UserUid(record.userId!!),
            platform = record.platform!!,
            givenId = UserId(record.givenId!!),
            name = record.name!!,
            uniqueName = record.uniqueName!!,
            announceId = record.announceId,
            profileURL = record.profileUrl
        )

}
