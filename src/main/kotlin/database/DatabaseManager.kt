package database

import database.entities.SimpleProfile
import database.entities.UserData
import database.entities.asSimpleProfile
import interact.i18n.Language
import interact.message.graphics.BoardStyle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import session.entities.GuildConfig
import utility.GuildId
import utility.UserId
import java.util.TreeSet

object DatabaseManager {

    private val rankingCache: TreeSet<SimpleProfile> = sortedSetOf()
    private val rankingCacheMutex: Mutex = Mutex()

    suspend fun fetchGuildConfig(connection: DatabaseConnection, guildId: GuildId): GuildConfig =
        GuildConfig(Language.ENG, BoardStyle.IMAGE) // TODO

    suspend fun updateGuildConfig(connection: DatabaseConnection, guildId: GuildId, guildConfig: GuildConfig): Unit = TODO()

    suspend fun fetchUserData(connection: DatabaseConnection, userId: UserId): UserData = TODO()

    suspend fun updateUserData(connection: DatabaseConnection, userId: UserId): Unit = TODO()

    suspend fun uploadGameRecord(connection: DatabaseConnection, ownerId: UserId, opponentId: UserId?): Unit = TODO()

    private suspend fun recalculateRanking(userData: UserData) {
        val profile = userData.asSimpleProfile()
        val bottomProfile = rankingCache.first()
        if (profile > bottomProfile) {
            rankingCacheMutex.withLock {
                rankingCache.remove(bottomProfile)
                rankingCache.add(profile)
            }
        }
    }

    private suspend fun fetchRankingCache(connection: DatabaseConnection): MutableSet<SimpleProfile> = TODO()

    suspend fun retrieveRanking(connection: DatabaseConnection): Set<SimpleProfile> =
        rankingCache.ifEmpty {
            fetchRankingCache(connection).also {
                rankingCacheMutex.withLock {
                    rankingCache.addAll(it)
                }
            }
        }

}
