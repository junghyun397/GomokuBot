package session

import database.DatabaseManager
import kotlinx.coroutines.sync.withLock
import session.entities.GameSession
import session.entities.GuildConfig
import session.entities.GuildSession
import session.entities.RequestSession
import utility.GuildId
import utility.UserId

object SessionManager {

    private suspend inline fun retrieveGuildSession(repo: SessionRepository, guildId: GuildId): GuildSession =
        repo.sessions.getOrPut(guildId.id) {
            GuildSession(guildConfig = DatabaseManager.fetchGuildConfig(repo.databaseConnection, guildId))
        }

    private suspend inline fun mapGuildSession(repo: SessionRepository, guildId: GuildId, mapper: (GuildSession) -> GuildSession): GuildSession =
        this.retrieveGuildSession(repo, guildId).let {
            mapper(it).also {
                repo.sessionsMutex.withLock {
                    repo.sessions[guildId.id] = mapper(it)
                }
            }
        }

    private suspend fun cleanEmptySessions(repo: SessionRepository): Unit =
        repo.sessionsMutex.withLock {
            repo.sessions
                .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
                .forEach { repo.sessions.remove(it.key) }
        }

    suspend fun retrieveGuildConfig(repo: SessionRepository, guildId: GuildId): GuildConfig =
        this.retrieveGuildSession(repo, guildId).guildConfig

    suspend fun updateGuildConfig(repo: SessionRepository, guildId: GuildId, guildConfig: GuildConfig): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(guildConfig = guildConfig)
        }.let {
            DatabaseManager.updateGuildConfig(repo.databaseConnection, guildId, it.guildConfig)
        }

    suspend fun retrieveRequestSessionByOwner(repo: SessionRepository, guildId: GuildId, ownerId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions[ownerId]

    suspend fun retrieveRequestSessionByOpponent(repo: SessionRepository, guildId: GuildId, opponentId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.opponentId == opponentId }

    suspend fun putRequestSession(repo: SessionRepository, guildId: GuildId, requestSession: RequestSession) {
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions + (requestSession.ownerId to requestSession))
        }
    }

    suspend fun removeRequestSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId) {
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun retrieveGameSessionById(repo: SessionRepository, guildId: GuildId, userId: UserId): GameSession? =
        this.retrieveGuildSession(repo, guildId).gameSessions
            .values
            .firstOrNull { it.ownerId == userId || it.opponent == userId }

    suspend fun putGameSession(repo: SessionRepository, guildId: GuildId, gameSession: GameSession) {
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions + (gameSession.ownerId to gameSession))
        }
    }

    suspend fun removeGameSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId) {
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }
    }

}
