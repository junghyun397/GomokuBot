package core.database.repositories

import core.assets.User
import core.assets.UserId
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.jooq.tables.records.UserProfileRecord
import core.database.jooq.tables.references.USER_PROFILE
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object UserProfileRepository {

    context(connection: DatabaseConnection)
    suspend fun retrieveOrInsertUser(platform: Short, givenId: UserId, produce: () -> User.Human): User.Human =
        this.retrieveUser(platform, givenId)
            ?: produce()
                .copy(announceId = AnnounceRepository.getLatestAnnounceId())
                .also { this.upsertUser(it) }

    context(connection: DatabaseConnection)
    suspend fun retrieveUser(userUid: UserUid): User.Human =
        connection.localCaches.userProfileUidCache
            .getIfPresent(userUid)
            ?: this.fetchUser(userUid)
                .also { this.cacheUser(it) }

    context(connection: DatabaseConnection)
    suspend fun retrieveUsers(userUids: Collection<UserUid>): Map<UserUid, User.Human> {
        val users = mutableMapOf<UserUid, User.Human>()

        val missingUserUids = userUids
            .asSequence()
            .map { it to connection.localCaches.userProfileUidCache.getIfPresent(it) }
            .onEach { (uid, record) -> if (record != null) users[uid] = record }
            .filter { (_, record) -> record == null }
            .map { (uid, _) -> uid }
            .toCollection(mutableSetOf())

        if (missingUserUids.isEmpty())
            return users

        Flux.from(
            connection.jooq
                .selectFrom(USER_PROFILE)
                .where(USER_PROFILE.USER_ID.`in`(missingUserUids.map { it.uuid }))
        )
            .map { this.extractUser(it) }
            .collectList()
            .awaitSingle()
            .forEach { user ->
                this.cacheUser(user)
                users[user.id] = user
            }

        return users
    }

    context(connection: DatabaseConnection)
    suspend fun retrieveUser(platform: Short, givenId: UserId): User.Human? {
        connection.localCaches.userProfileGivenIdCache
            .getIfPresent(givenId)
            ?.let { return it }

        val maybeUser = this.fetchUser(platform, givenId)

        if (maybeUser != null)
            this.cacheUser(maybeUser)

        return maybeUser
    }

    context(connection: DatabaseConnection)
    private suspend fun fetchUser(userUid: UserUid): User.Human =
        Mono.from(
            connection.jooq
                .selectFrom(USER_PROFILE)
                .where(USER_PROFILE.USER_ID.eq(userUid.uuid))
        )
            .map { this.extractUser(it) }
            .awaitSingle()

    context(connection: DatabaseConnection)
    private suspend fun fetchUser(platform: Short, givenId: UserId): User.Human? =
        Mono.from(
            connection.jooq
                .selectFrom(USER_PROFILE)
                .where(USER_PROFILE.PLATFORM.eq(platform))
                .and(USER_PROFILE.GIVEN_ID.eq(givenId.idLong))
        )
            .map { this.extractUser(it) }
            .awaitSingleOrNull()

    context(connection: DatabaseConnection)
    suspend fun upsertUser(user: User.Human) {
        this.cacheUser(user)

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

    context(connection: DatabaseConnection)
    private fun cacheUser(user: User.Human) {
        connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
        connection.localCaches.userProfileUidCache.put(user.id, user)
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
