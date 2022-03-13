package session

import interact.i18n.LanguageContainer
import kotlinx.coroutines.sync.withLock
import session.entities.GameSession
import session.entities.GuildConfig
import session.entities.GuildSession
import session.entities.RequestSession
import utility.GuildId
import utility.UserId

object SessionManager {

    private suspend fun retrieveGuildSession(repo: SessionRepository, guildId: GuildId): GuildSession =
        repo.sessions.getOrElse(guildId.id) {
            GuildSession() // TODO()
        }

    private suspend fun mapGuildSession(repo: SessionRepository, guildId: GuildId, mapper: (GuildSession) -> GuildSession): Unit =
        this.retrieveGuildSession(repo, guildId).let {
            repo.sessionsMutex.withLock {
                repo.sessions[guildId.id] = mapper(it)
            }
        }

    private fun cleanEmptySessions(repo: SessionRepository): Unit =
        repo.sessions
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { repo.sessions.remove(it.key) }

    suspend fun retrieveLanguageContainer(repo: SessionRepository, guildId: GuildId): LanguageContainer =
        this.retrieveGuildSession(repo, guildId).languageContainer

    suspend fun updateLanguageContainer(repo: SessionRepository, guildId: GuildId, languageContainer: LanguageContainer): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(languageContainer = languageContainer)
        }

    suspend fun retrieveGuildConfig(repo: SessionRepository, guildId: GuildId): GuildConfig =
        this.retrieveGuildSession(repo, guildId).guildConfig

    suspend fun updateGuildConfig(repo: SessionRepository, guildId: GuildId, guildConfig: GuildConfig): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(guildConfig = guildConfig)
        }

    suspend fun retrieveRequestSessionByOwner(repo: SessionRepository, guildId: GuildId, ownerId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.ownerId == ownerId }

    suspend fun retrieveRequestSessionByOpponent(repo: SessionRepository, guildId: GuildId, opponentId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.opponentId == opponentId }

    suspend fun putRequestSession(repo: SessionRepository, guildId: GuildId, requestSession: RequestSession): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions + (requestSession.ownerId to requestSession))
        }

    suspend fun removeRequestSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }

    suspend fun retrieveGameSessionById(repo: SessionRepository, guildId: GuildId, userId: UserId): GameSession? =
        this.retrieveGuildSession(repo, guildId).gameSessions
            .values
            .firstOrNull { it.ownerId == userId || it.opponent == userId }

    suspend fun putGameSession(repo: SessionRepository, guildId: GuildId, gameSession: GameSession): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions + (gameSession.ownerId to gameSession))
        }

    suspend fun removeGameSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId): Unit =
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }

}
