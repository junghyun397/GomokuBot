package core.session

import kotlinx.coroutines.sync.withLock
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.GuildSession
import core.session.entities.RequestSession
import utils.values.GuildId
import utils.values.LinuxTime
import utils.values.UserId

object SessionManager {

    private suspend inline fun retrieveGuildSession(repo: SessionRepository, guildId: GuildId): GuildSession =
        repo.sessions.getOrPut(guildId) {
            GuildSession(guildConfig = core.database.DatabaseManager.fetchGuildConfig(repo.databaseConnection, guildId))
        }

    private suspend inline fun mapGuildSession(repo: SessionRepository, guildId: GuildId, mapper: (GuildSession) -> GuildSession): GuildSession =
        mapper(this.retrieveGuildSession(repo, guildId)).also {
            repo.sessionsMutex.withLock {
                repo.sessions[guildId] = it
            }
        }

    suspend fun retrieveGuildConfig(repo: SessionRepository, guildId: GuildId): GuildConfig =
        this.retrieveGuildSession(repo, guildId).guildConfig

    suspend fun updateGuildConfig(repo: SessionRepository, guildId: GuildId, guildConfig: GuildConfig) {
        this.mapGuildSession(repo, guildId) {
            it.copy(guildConfig = guildConfig)
        }.also {
            core.database.DatabaseManager.updateGuildConfig(repo.databaseConnection, guildId, it.guildConfig)
        }
    }

    suspend fun retrieveRequestSession(repo: SessionRepository, guildId: GuildId, userId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.ownerId == userId || it.opponentId == userId }

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

    private suspend fun cleanExpiredSessions(repo: SessionRepository): Unit =
        repo.sessionsMutex.withLock {
            val referenceTime = LinuxTime()
            repo.sessions
                .map {
                    Triple(
                        first = it,
                        second = it.value.gameSessions.filter { session -> session.value.expireDate > referenceTime },
                        third = it.value.requestSessions.filter { session -> session.value.expireDate > referenceTime }
                    )
                }
                .filter {
                    it.first.value.gameSessions.size != it.second.size || it.first.value.requestSessions.size != it.third.size
                }
                .forEach {
                    repo.sessions[it.first.key] = it.first.value.copy(
                        gameSessions = it.second,
                        requestSessions = it.third
                    )
                }
        }

    private suspend fun cleanEmptySessions(repo: SessionRepository): Unit =
        repo.sessionsMutex.withLock {
            repo.sessions
                .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
                .forEach { repo.sessions.remove(it.key) }
        }

}
