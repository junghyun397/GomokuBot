package session

import interact.i18n.LanguageContainer
import session.entities.GameSession
import session.entities.GuildSession
import session.entities.RequestSession
import utility.GuildId
import utility.UserId

object SessionManager {

    private suspend fun retrieveGuildSession(repo: SessionRepository, guildId: GuildId): GuildSession =
        repo.sessions.getOrElse(guildId.id) {
            GuildSession() // TODO()
        }

    private suspend fun updateGuildSession(repo: SessionRepository, guildId: GuildId, block: (GuildSession) -> GuildSession): Unit =
        this.retrieveGuildSession(repo, guildId).let {
            repo.sessions[guildId.id] = block(it)
        }

    private fun cleanEmptySessions(repo: SessionRepository): Unit =
        repo.sessions
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { repo.sessions.remove(it.key) }

    suspend fun retrieveLanguageContainer(repo: SessionRepository, guildId: GuildId): LanguageContainer =
        this.retrieveGuildSession(repo, guildId).languageContainer

    suspend fun updateLanguageContainer(repo: SessionRepository, guildId: GuildId, languageContainer: LanguageContainer): Unit =
        this.updateGuildSession(repo, guildId) {
            it.copy(languageContainer = languageContainer)
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
        this.updateGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions + (requestSession.ownerId to requestSession))
        }

    suspend fun removeRequestSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId): Unit =
        this.updateGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }

    suspend fun retrieveGameSessionById(repo: SessionRepository, guildId: GuildId, userId: UserId): GameSession? =
        this.retrieveGuildSession(repo, guildId).gameSessions
            .values
            .firstOrNull { it.ownerId == userId || it.opponent == userId }

    suspend fun putGameSession(repo: SessionRepository, guildId: GuildId, gameSession: GameSession): Unit =
        this.updateGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions + (gameSession.ownerId to gameSession))
        }

    suspend fun removeGameSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId): Unit =
        this.updateGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }

}
