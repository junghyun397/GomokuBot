package core.session

import core.assets.GuildId
import core.assets.Message
import core.assets.UserId
import core.database.DatabaseManager
import core.session.entities.*
import utils.assets.LinuxTime

object SessionManager {

    private suspend inline fun retrieveGuildSession(repo: SessionRepository, guildId: GuildId): GuildSession =
        repo.sessions.getOrElse(guildId) {
            DatabaseManager.fetchGuildConfig(repo.databaseConnection, guildId)
                .fold(
                    onDefined = { GuildSession(guildConfig = it) },
                    onEmpty = { GuildSession(GuildConfig(guildId)) }
                )
        }

    private suspend inline fun mapGuildSession(repo: SessionRepository, guildId: GuildId, mapper: (GuildSession) -> GuildSession): GuildSession =
        mapper(this.retrieveGuildSession(repo, guildId)).also {
            repo.sessions[guildId] = it
        }

    suspend fun retrieveGuildConfig(repo: SessionRepository, guildId: GuildId): GuildConfig =
        this.retrieveGuildSession(repo, guildId).guildConfig

    suspend fun updateGuildConfig(repo: SessionRepository, guildId: GuildId, guildConfig: GuildConfig) {
        this.mapGuildSession(repo, guildId) {
            it.copy(guildConfig = guildConfig)
        }.also {
            DatabaseManager.updateGuildConfig(repo.databaseConnection, guildId, it.guildConfig)
        }
    }

    suspend fun retrieveRequestSession(repo: SessionRepository, guildId: GuildId, userId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.owner.id == userId || it.opponent.id == userId }

    suspend fun retrieveRequestSessionByOwner(repo: SessionRepository, guildId: GuildId, ownerId: UserId): RequestSession? =
        this.retrieveGuildSession(repo, guildId).requestSessions[ownerId]

    suspend fun putRequestSession(repo: SessionRepository, guildId: GuildId, requestSession: RequestSession) {
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions + (requestSession.owner.id to requestSession))
        }
    }

    suspend fun removeRequestSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId) {
        this.mapGuildSession(repo, guildId) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun retrieveGameSession(repo: SessionRepository, guildId: GuildId, userId: UserId): GameSession? =
        this.retrieveGuildSession(repo, guildId).gameSessions
            .values
            .firstOrNull { it.owner.id == userId || if (it is PvpGameSession) it.opponent.id == userId else false }

    suspend fun putGameSession(repo: SessionRepository, guildId: GuildId, gameSession: GameSession) {
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions + (gameSession.owner.id to gameSession))
        }
    }

    suspend fun removeGameSession(repo: SessionRepository, guildId: GuildId, ownerId: UserId) {
        this.mapGuildSession(repo, guildId) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }
    }

    fun appendMessage(repo: SessionRepository, key: String, message: Message) =
        repo.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(message)

    fun checkoutMessages(repo: SessionRepository, key: String): List<Message>? =
        repo.messageBuffer.remove(key)

    fun cleanExpiredSessions(repo: SessionRepository) {
        val referenceTime = LinuxTime()
        repo.sessions
            .asSequence()
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

    fun cleanEmptySessions(repo: SessionRepository) {
        repo.sessions
            .asSequence()
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { repo.sessions.remove(it.key) }
    }

}
