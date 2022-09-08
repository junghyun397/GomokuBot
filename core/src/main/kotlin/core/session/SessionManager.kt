package core.session

import core.assets.*
import core.database.repositories.GuildConfigRepository
import core.session.entities.*
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.Quadruple
import utils.structs.fold

object SessionManager {

    private suspend fun retrieveGuildSession(repo: SessionRepository, guild: Guild): GuildSession =
        repo.sessions.getOrElse(guild.id) {
            GuildConfigRepository.fetchGuildConfig(repo.dbConnection, guild.id)
                .fold(
                    onDefined = { GuildSession(guild = guild, config = it) },
                    onEmpty = { GuildSession(guild = guild, GuildConfig()) }
                )
                .also {
                    repo.sessions[guild.id] = it
                }
        }

    private suspend inline fun mapGuildSession(repo: SessionRepository, guild: Guild, mapper: (GuildSession) -> GuildSession): GuildSession =
        mapper(this.retrieveGuildSession(repo, guild)).also {
            repo.sessions[guild.id] = it
        }

    suspend fun retrieveGuildConfig(repo: SessionRepository, guild: Guild): GuildConfig =
        this.retrieveGuildSession(repo, guild).config

    suspend fun updateGuildConfig(repo: SessionRepository, guild: Guild, guildConfig: GuildConfig) {
        this.mapGuildSession(repo, guild) {
            it.copy(config = guildConfig)
        }.also {
            GuildConfigRepository.upsertGuildConfig(repo.dbConnection, guild.id, it.config)
        }
    }

    suspend fun hasRequestSession(repo: SessionRepository, guild: Guild, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveGuildSession(repo, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveRequestSession(repo: SessionRepository, guild: Guild, userUid: UserUid): RequestSession? =
        this.retrieveGuildSession(repo, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == userUid || it.opponent.id == userUid }

    suspend fun retrieveRequestSessionByOwner(repo: SessionRepository, guild: Guild, ownerId: UserUid): RequestSession? =
        this.retrieveGuildSession(repo, guild).requestSessions[ownerId]

    suspend fun retrieveRequestSessionByOpponent(repo: SessionRepository, guild: Guild, opponentId: UserUid): RequestSession? =
        this.retrieveGuildSession(repo, guild).requestSessions.values.firstOrNull { it.opponent.id == opponentId }

    suspend fun putRequestSession(repo: SessionRepository, guild: Guild, requestSession: RequestSession) {
        this.mapGuildSession(repo, guild) {
            it.copy(requestSessions = it.requestSessions + (requestSession.owner.id and requestSession))
        }
    }

    suspend fun removeRequestSession(repo: SessionRepository, guild: Guild, ownerId: UserUid) {
        this.mapGuildSession(repo, guild) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun hasGameSession(repo: SessionRepository, guild: Guild, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveGuildSession(repo, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveGameSession(repo: SessionRepository, guild: Guild, userUid: UserUid): GameSession? =
        this.retrieveGuildSession(repo, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == userUid || if (it is PvpGameSession) it.opponent.id == userUid else false }

    suspend fun putGameSession(repo: SessionRepository, guild: Guild, gameSession: GameSession) {
        this.mapGuildSession(repo, guild) {
            it.copy(gameSessions = it.gameSessions + (gameSession.owner.id and gameSession))
        }
    }

    suspend fun removeGameSession(repo: SessionRepository, guild: Guild, ownerId: UserUid) {
        this.mapGuildSession(repo, guild) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }
    }

    fun generateMessageBufferKey(owner: User): String =
        String(owner.name.toCharArray() + System.currentTimeMillis().toString().toCharArray())

    fun appendMessageHead(repo: SessionRepository, key: String, messageRef: MessageRef) {
        repo.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(0, messageRef)
    }

    fun appendMessage(repo: SessionRepository, key: String, messageRef: MessageRef) {
        repo.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(messageRef)
    }

    fun viewHeadMessage(repo: SessionRepository, key: String): MessageRef? =
        repo.messageBuffer[key]?.first()

    fun viewTailMessage(repo: SessionRepository, key: String): MessageRef? =
        repo.messageBuffer[key]?.last()

    fun checkoutMessages(repo: SessionRepository, key: String): List<MessageRef>? =
        repo.messageBuffer.remove(key)

    fun addNavigate(repo: SessionRepository, messageRef: MessageRef, state: NavigateState) {
        repo.navigates[messageRef] = state
    }

    fun getNavigateState(repo: SessionRepository, messageRef: MessageRef): NavigateState? =
        repo.navigates[messageRef]

    private inline fun <T : Expirable> cleanExpired(
        repo: SessionRepository,
        crossinline extract: (GuildSession) -> Map<UserUid, T>,
        crossinline mutate: (GuildSession, Set<UserUid>) -> GuildSession
    ): Sequence<Quadruple<GuildUid, GuildSession, UserUid, T>> =
        LinuxTime().let { referenceTime ->
            repo.sessions
                .asSequence()
                .flatMap { (guildId, session) ->
                    extract(session)
                        .filter { referenceTime.timestamp > it.value.expireDate.timestamp }
                        .map { (userId, expired) -> Quadruple(guildId, session, userId, expired) }
                        .also { expires ->
                            repo.sessions[guildId] = mutate(repo.sessions[guildId]!!, expires.map { it.third }.toSet())
                        }
                }
        }

    fun cleanExpiredRequestSessions(repo: SessionRepository): Sequence<Quadruple<GuildUid, GuildSession, UserUid, RequestSession>> =
        this.cleanExpired(repo,
            extract = { it.requestSessions },
            mutate = { original, exclude -> original.copy(requestSessions = original.requestSessions - exclude) }
        )

    fun cleanExpiredGameSession(repo: SessionRepository): Sequence<Quadruple<GuildUid, GuildSession, UserUid, GameSession>> =
        this.cleanExpired(repo,
            extract = { it.gameSessions },
            mutate = { original, exclude -> original.copy(gameSessions = original.gameSessions - exclude) }
        )

    fun cleanEmptySessions(repo: SessionRepository) {
        repo.sessions
            .asSequence()
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { repo.sessions.remove(it.key) }
    }

    fun cleanExpiredNavigators(repo: SessionRepository): Map<MessageRef, NavigateState> {
        val referenceTime = LinuxTime()

        return repo.navigates
            .filterValues { referenceTime.timestamp > it.expireDate.timestamp }
            .onEach { repo.navigates.remove(it.key) }
    }

}
