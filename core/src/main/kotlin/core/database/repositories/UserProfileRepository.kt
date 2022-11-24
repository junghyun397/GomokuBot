package core.database.repositories

import core.assets.User
import core.assets.UserId
import core.assets.UserUid
import core.assets.bindNullable
import core.database.DatabaseConnection
import kotlinx.coroutines.reactive.awaitSingle
import utils.structs.*
import java.util.*

object UserProfileRepository {

    suspend fun retrieveOrInsertUser(connection: DatabaseConnection, platform: Short, givenId: UserId, produce: () -> User): User =
        this.retrieveUser(connection, platform, givenId)
            .orElseGet {
                produce()
                    .copy(announceId = AnnounceRepository.getLatestAnnounceId(connection))
                    .also { this.upsertUser(connection, it) }
            }

    suspend fun retrieveUser(connection: DatabaseConnection, userUid: UserUid): User =
        connection.localCaches.userProfileUidCache
            .getIfPresent(userUid)
            .asOption()
            .orElseGet {
                this.fetchUser(connection, userUid)
                    .also { user ->
                        connection.localCaches.userProfileUidCache.put(user.id, user)
                        connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
                    }
            }

    suspend fun retrieveUser(connection: DatabaseConnection, platform: Short, givenId: UserId): Option<User> =
        connection.localCaches.userProfileGivenIdCache
            .getIfPresent(givenId)
            .asOption()
            .orElse {
                this.fetchUser(connection, platform, givenId)
                    .also { fetched -> fetched
                        .forEach { user ->
                            connection.localCaches.userProfileUidCache.put(user.id, user)
                            connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
                        }
                    }
            }

    private suspend fun fetchUser(connection: DatabaseConnection, userUid: UserUid): User =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM user_profile WHERE user_id = $1")
                .bind("$1", userUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    User(
                        id = userUid,
                        platform = row["platform"] as Short,
                        givenId = UserId(row["given_id"] as Long),
                        name = row["name"] as String,
                        nameTag = row["name_tag"] as String,
                        announceId = row["announce_id"] as Int?,
                        profileURL = row["profile_url"] as String?
                    )
                }
            }
            .awaitSingle()

    private suspend fun fetchUser(connection: DatabaseConnection, platform: Short, givenId: UserId): Option<User> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM user_profile WHERE platform = $1 AND given_id = $2")
                .bind("$1", platform)
                .bind("$2", givenId.idLong)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Option(User(
                        id = UserUid(row["user_id"] as UUID),
                        platform = platform,
                        givenId = givenId,
                        name = row["name"] as String,
                        nameTag = row["name_tag"] as String,
                        announceId = row["announce_id"] as Int?,
                        profileURL = row["profile_url"] as String?
                    ))
                }
            }
            .defaultIfEmpty(Option.Empty)
            .awaitSingle()

    suspend fun upsertUser(connection: DatabaseConnection, user: User) {
        connection.localCaches.userProfileGivenIdCache.put(user.givenId, user)
        connection.localCaches.userProfileUidCache.put(user.id, user)

        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        INSERT INTO user_profile (user_id, platform, given_id, name, name_tag, announce_id, profile_url) VALUES ($1, $2, $3, $4, $5, $6, $7)
                            ON CONFLICT (user_id) DO UPDATE SET platform = $2, name = $4, name_tag = $5, announce_id = $6, profile_url = $7
                    """.trimIndent()
                )
                .bind("$1", user.id.uuid)
                .bind("$2", user.platform)
                .bind("$3", user.givenId.idLong)
                .bind("$4", user.name)
                .bind("$5", user.nameTag)
                .bindNullable("$6", user.announceId)
                .bindNullable("$7", user.profileURL)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitSingle()
    }

}
