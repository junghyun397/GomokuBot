package database

import database.entities.SimpleProfile
import database.entities.UserData
import database.entities.toSimpleProfile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import session.entities.GuildConfig
import utility.GuildId
import utility.UserId
import java.util.TreeSet

object DatabaseManager {

    private val rankingCache: TreeSet<SimpleProfile> = sortedSetOf()
    private val rankingCacheMutex: Mutex = Mutex()

    suspend fun fetchGuildConfig(connection: DatabaseConnection, guildId: GuildId): GuildConfig = TODO()

    suspend fun updateGuildConfig(connection: DatabaseConnection, guildId: GuildId, guildConfig: GuildConfig): Unit = TODO()

    suspend fun fetchUserData(connection: DatabaseConnection, userId: UserId): UserData = TODO()

    suspend fun updateUserData(connection: DatabaseConnection, userId: UserId): Unit = TODO()

    suspend fun uploadGameRecord(connection: DatabaseConnection, ownerId: UserId, opponentId: UserId?): Unit = TODO()

    private suspend fun recalculateRanking(userData: UserData): Unit =
        userData.toSimpleProfile().let { profile ->
            rankingCache.first().let { bottomProfile ->
                if (profile > bottomProfile) {
                    rankingCacheMutex.withLock {
                        rankingCache.remove(bottomProfile)
                        rankingCache.add(profile)
                    }
                }
            }
        }

    private suspend fun fetchRankingCache(connection: DatabaseConnection): MutableSet<SimpleProfile> = TODO()

    suspend fun retrieveRanking(connection: DatabaseConnection): Set<SimpleProfile> =
        rankingCache.also {
            it.ifEmpty { rankingCacheMutex.withLock {
                rankingCache.addAll(fetchRankingCache(connection))
            } }
        }

}
