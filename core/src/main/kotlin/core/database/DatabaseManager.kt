package core.database

import core.assets.GuildId
import core.assets.UserId
import core.database.entities.SimpleProfile
import core.database.entities.UserData
import core.database.entities.asSimpleProfile
import core.session.entities.GuildConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.structs.Option
import java.util.*

object DatabaseManager {

    private val rankingCache: TreeSet<SimpleProfile> = sortedSetOf()
    private val rankingCacheMutex: Mutex = Mutex()

    suspend fun fetchGuildConfig(connection: DatabaseConnection, guildId: GuildId): Option<GuildConfig> =
        Option(GuildConfig(guildId)) // TODO

    suspend fun updateGuildConfig(connection: DatabaseConnection, guildId: GuildId, guildConfig: GuildConfig): Unit = Unit

    suspend fun fetchUserData(connection: DatabaseConnection, userId: UserId): UserData = TODO()

    suspend fun updateUserData(connection: DatabaseConnection, userId: UserId): Unit = TODO()

    suspend fun uploadGameRecord(connection: DatabaseConnection, ownerId: UserId, opponentId: UserId?): Unit = TODO()

    private suspend fun recalculateRanking(userData: UserData) {
        val profile = userData.asSimpleProfile()
        val bottomProfile = this.rankingCache.first()
        if (profile > bottomProfile) {
            this.rankingCacheMutex.withLock {
                this.rankingCache.remove(bottomProfile)
                this.rankingCache.add(profile)
            }
        }
    }

    private suspend fun fetchRankingCache(connection: DatabaseConnection): MutableSet<SimpleProfile> = TODO()

    suspend fun retrieveRanking(connection: DatabaseConnection, takeN: Int): List<SimpleProfile> =
        this.rankingCache.ifEmpty {
            this.fetchRankingCache(connection).also {
                this.rankingCacheMutex.withLock {
                    this.rankingCache.addAll(it)
                }
            }
        }.take(takeN)

}
