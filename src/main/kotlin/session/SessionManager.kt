package session

import interact.i18n.LanguageContainer
import session.entities.GameSession
import session.entities.GuildSession
import session.entities.RequestSession
import utility.GuildId
import utility.UserId

object SessionManager {

    private val guildIdMap: MutableMap<Long, GuildSession> = mutableMapOf()

    private suspend fun fetchGuildSession(guildId: GuildId): GuildSession =
        this.guildIdMap.getOrElse(guildId.id) {
            TODO()
        }

    private suspend fun updateGuildSession(guildId: GuildId, block: (GuildSession) -> GuildSession): Unit =
        this.fetchGuildSession(guildId).let {
            this.guildIdMap[guildId.id] = block(it)
        }

    private fun cleanEmptySessions(): Unit =
        this.guildIdMap
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { this.guildIdMap.remove(it.key) }

    suspend fun getLanguageContainer(guildId: GuildId): LanguageContainer =
        this.fetchGuildSession(guildId).languageContainer

    suspend fun updateLanguageContainer(guildId: GuildId, languageContainer: LanguageContainer): Unit =
        this.updateGuildSession(guildId) {
            it.copy(languageContainer = languageContainer)
        }

    suspend fun getRequestSessionByOwner(guildId: GuildId, ownerId: UserId): RequestSession? =
        this.fetchGuildSession(guildId).requestSessions
            .values
            .firstOrNull { it.ownerId == ownerId }

    suspend fun getRequestSessionByOpponent(guildId: GuildId, opponentId: UserId): RequestSession? =
        this.fetchGuildSession(guildId).requestSessions
            .values
            .firstOrNull { it.opponentId == opponentId }

    suspend fun putRequestSession(guildId: GuildId, requestSession: RequestSession): Unit =
        this.updateGuildSession(guildId) {
            it.copy(requestSessions = it.requestSessions + (requestSession.ownerId to requestSession))
        }

    suspend fun removeRequestSession(guildId: GuildId, ownerId: UserId): Unit =
        this.updateGuildSession(guildId) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }

    suspend fun getGameSessionById(guildId: GuildId, userId: UserId): GameSession? =
        this.fetchGuildSession(guildId).gameSessions
            .values
            .firstOrNull { it.ownerId == userId || it.opponent == userId }

    suspend fun putGameSession(guildId: GuildId, gameSession: GameSession): Unit =
        this.updateGuildSession(guildId) {
            it.copy(gameSessions = it.gameSessions + (gameSession.ownerId to gameSession))
        }

    suspend fun removeGameSession(guildId: GuildId, ownerId: UserId): Unit =
        this.updateGuildSession(guildId) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }

}