package core.database

import core.database.entities.SimpleProfile
import core.database.entities.UserData
import core.database.entities.asSimpleProfile
import core.interact.i18n.Language
import core.interact.message.graphics.BoardStyle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import core.session.entities.GuildConfig
import utils.values.GuildId
import utils.values.UserId
import java.util.TreeSet

object DatabaseManager {

    private val rankingCache: TreeSet<SimpleProfile> = sortedSetOf()
    private val rankingCacheMutex: Mutex = Mutex()

    suspend fun fetchGuildConfig(connection: core.database.DatabaseConnection, guildId: GuildId): GuildConfig =
        GuildConfig(Language.ENG, BoardStyle.IMAGE) // TODO

    suspend fun updateGuildConfig(connection: core.database.DatabaseConnection, guildId: GuildId, guildConfig: GuildConfig): Unit = TODO()

    suspend fun fetchUserData(connection: core.database.DatabaseConnection, userId: UserId): UserData = TODO()

    suspend fun updateUserData(connection: core.database.DatabaseConnection, userId: UserId): Unit = TODO()

    suspend fun uploadGameRecord(connection: core.database.DatabaseConnection, ownerId: UserId, opponentId: UserId?): Unit = TODO()

    private suspend fun recalculateRanking(userData: UserData) {
        val profile = userData.asSimpleProfile()
        val bottomProfile = core.database.DatabaseManager.rankingCache.first()
        if (profile > bottomProfile) {
            core.database.DatabaseManager.rankingCacheMutex.withLock {
                core.database.DatabaseManager.rankingCache.remove(bottomProfile)
                core.database.DatabaseManager.rankingCache.add(profile)
            }
        }
    }

    private suspend fun fetchRankingCache(connection: core.database.DatabaseConnection): MutableSet<SimpleProfile> = TODO()

    suspend fun retrieveRanking(connection: core.database.DatabaseConnection): Set<SimpleProfile> =
        core.database.DatabaseManager.rankingCache.ifEmpty {
            core.database.DatabaseManager.fetchRankingCache(connection).also {
                core.database.DatabaseManager.rankingCacheMutex.withLock {
                    core.database.DatabaseManager.rankingCache.addAll(it)
                }
            }
        }

}
