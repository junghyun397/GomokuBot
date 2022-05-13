package core.session

import core.assets.GuildId
import core.assets.Message
import core.assets.User
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

    suspend fun hasRequestSession(repo: SessionRepository, guildId: GuildId, user1: UserId, user2: UserId) =
        this.retrieveGuildSession(repo, guildId).requestSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

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

    suspend fun hasGameSession(repo: SessionRepository, guildId: GuildId, user1: UserId, user2: UserId) =
        this.retrieveGuildSession(repo, guildId).gameSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

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

    fun generateMessageBufferKey(owner: User) =
        String(owner.name.toCharArray() + System.currentTimeMillis().toString().toCharArray())

    fun appendMessageHead(repo: SessionRepository, key: String, message: Message) =
        repo.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(0, message)

    fun appendMessage(repo: SessionRepository, key: String, message: Message) =
        repo.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(message)

    fun getHeadMessage(repo: SessionRepository, key: String): Message? =
        repo.messageBuffer[key]?.first()

    fun checkoutMessages(repo: SessionRepository, key: String): List<Message>? =
        repo.messageBuffer.remove(key)

    fun addNavigate(repo: SessionRepository, message: Message, state: NavigateState) {
        repo.navigates[message] = state
    }

    fun getNavigateState(repo: SessionRepository, message: Message): NavigateState? =
        repo.navigates[message]

    private inline fun <T : Expirable> cleanExpired(
        repo: SessionRepository,
        crossinline extract: (GuildSession) -> Map<UserId, T>,
        crossinline mutate: (GuildSession, Set<UserId>) -> GuildSession
    ): List<Triple<GuildConfig, UserId, T>> =
        LinuxTime().let { referenceTime ->
            repo.sessions
                .asSequence()
                .flatMap { entry ->
                    extract(entry.value)
                        .filter { it.value.expireDate.timestamp > referenceTime.timestamp }
                        .map { Triple(entry.value.guildConfig, it.key, it.value) }
                }
                .toList()
        }.also { expired ->
            expired
                .groupBy { it.first.id }
                .forEach { entry ->
                    repo.sessions[entry.key] = mutate(repo.sessions[entry.key]!!, entry.value.map { it.second }.toSet())
                }
        }

    fun cleanExpiredRequestSessions(repo: SessionRepository) =
        this.cleanExpired(repo,
            extract = { it.requestSessions },
            mutate = { original, exclude -> original.copy(requestSessions = original.requestSessions - exclude) }
        )

    fun cleanExpiredGameSession(repo: SessionRepository) =
        this.cleanExpired(repo,
            extract = { it.gameSessions },
            mutate = { original, exclude -> original.copy(gameSessions = original.gameSessions - exclude) }
        )

    fun cleanEmptySessions(repo: SessionRepository) =
        repo.sessions
            .asSequence()
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { repo.sessions.remove(it.key) }

}
