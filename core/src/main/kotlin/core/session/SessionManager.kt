package core.session

import core.assets.Guild
import core.assets.GuildUid
import core.assets.MessageRef
import core.assets.UserUid
import core.database.repositories.GuildConfigRepository
import core.session.entities.*
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.Quadruple

object SessionManager {

    private suspend fun retrieveGuildSession(pool: SessionPool, guild: Guild): GuildSession =
        pool.sessions.getOrElse(guild.id) {
            GuildConfigRepository.fetchGuildConfig(pool.dbConnection, guild.id)
                .fold(
                    ifSome = { GuildSession(guild = guild, config = it) },
                    ifEmpty = { GuildSession(guild = guild, GuildConfig()) }
                )
                .also {
                    pool.sessions[guild.id] = it
                }
        }

    private suspend inline fun mapGuildSession(pool: SessionPool, guild: Guild, mapper: (GuildSession) -> GuildSession): GuildSession =
        mapper(this.retrieveGuildSession(pool, guild)).also {
            pool.sessions[guild.id] = it
        }

    suspend fun retrieveGuildConfig(pool: SessionPool, guild: Guild): GuildConfig =
        this.retrieveGuildSession(pool, guild).config

    suspend fun updateGuildConfig(pool: SessionPool, guild: Guild, guildConfig: GuildConfig) {
        this.mapGuildSession(pool, guild) {
            it.copy(config = guildConfig)
        }.also {
            GuildConfigRepository.upsertGuildConfig(pool.dbConnection, guild.id, it.config)
        }
    }

    suspend fun hasRequestSession(pool: SessionPool, guild: Guild, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveGuildSession(pool, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveRequestSession(pool: SessionPool, guild: Guild, userUid: UserUid): RequestSession? =
        this.retrieveGuildSession(pool, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == userUid || it.opponent.id == userUid }

    suspend fun retrieveRequestSessionByOwner(pool: SessionPool, guild: Guild, ownerId: UserUid): RequestSession? =
        this.retrieveGuildSession(pool, guild).requestSessions[ownerId]

    suspend fun retrieveRequestSessionByOpponent(pool: SessionPool, guild: Guild, opponentId: UserUid): RequestSession? =
        this.retrieveGuildSession(pool, guild).requestSessions.values.firstOrNull { it.opponent.id == opponentId }

    suspend fun putRequestSession(pool: SessionPool, guild: Guild, requestSession: RequestSession) {
        this.mapGuildSession(pool, guild) {
            it.copy(requestSessions = it.requestSessions + (requestSession.owner.id to requestSession))
        }
    }

    suspend fun removeRequestSession(pool: SessionPool, guild: Guild, ownerId: UserUid) {
        this.mapGuildSession(pool, guild) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun hasGameSession(pool: SessionPool, guild: Guild, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveGuildSession(pool, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveGameSession(pool: SessionPool, guild: Guild, userUid: UserUid): GameSession? =
        this.retrieveGuildSession(pool, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == userUid || it.opponent.id == userUid }

    suspend fun putGameSession(pool: SessionPool, guild: Guild, gameSession: GameSession) {
        this.mapGuildSession(pool, guild) {
            it.copy(gameSessions = it.gameSessions + (gameSession.owner.id to gameSession))
        }
    }

    suspend fun removeGameSession(pool: SessionPool, guild: Guild, ownerId: UserUid) {
        this.mapGuildSession(pool, guild) {
            it.copy(gameSessions = it.gameSessions - ownerId)
        }
    }

    fun appendMessageHead(pool: SessionPool, key: MessageBufferKey, messageRef: MessageRef) {
        pool.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(0, messageRef)
    }

    fun appendMessage(pool: SessionPool, key: MessageBufferKey, messageRef: MessageRef) {
        pool.messageBuffer.getOrPut(key) { mutableListOf() }
            .add(messageRef)
    }

    fun viewHeadMessage(pool: SessionPool, key: MessageBufferKey): MessageRef? =
        pool.messageBuffer[key]?.first()

    fun checkoutMessages(pool: SessionPool, key: MessageBufferKey): List<MessageRef>? =
        pool.messageBuffer.remove(key)

    fun addNavigation(pool: SessionPool, messageRef: MessageRef, state: NavigationState) {
        pool.navigates[messageRef] = state
    }

    fun getNavigationState(pool: SessionPool, messageRef: MessageRef): NavigationState? =
        pool.navigates[messageRef]

    private inline fun <T : Expirable> cleanExpired(
        pool: SessionPool,
        crossinline extract: (GuildSession) -> Map<UserUid, T>,
        crossinline mutate: (GuildSession, Set<UserUid>) -> GuildSession
    ): Sequence<Quadruple<GuildUid, GuildSession, UserUid, T>> {
        val referenceTime = LinuxTime.now()

        return pool.sessions
            .asSequence()
            .flatMap { (guildId, session) ->
                extract(session)
                    .filter { referenceTime.timestamp > it.value.expireDate.timestamp }
                    .map { (userId, expired) -> tuple(guildId, session, userId, expired) }
                    .also { expires ->
                        pool.sessions[guildId] = mutate(pool.sessions[guildId]!!, expires.map { it.third }.toSet())
                    }
            }
    }

    fun cleanExpiredRequestSessions(pool: SessionPool): Sequence<Quadruple<GuildUid, GuildSession, UserUid, RequestSession>> =
        this.cleanExpired(pool,
            extract = { it.requestSessions },
            mutate = { original, exclude -> original.copy(requestSessions = original.requestSessions - exclude) }
        )

    fun cleanExpiredGameSession(pool: SessionPool): Sequence<Quadruple<GuildUid, GuildSession, UserUid, GameSession>> =
        this.cleanExpired(pool,
            extract = { it.gameSessions },
            mutate = { original, exclude -> original.copy(gameSessions = original.gameSessions - exclude) }
        )

    fun cleanEmptySessions(pool: SessionPool) {
        pool.sessions
            .asSequence()
            .filter { it.value.requestSessions.isEmpty() && it.value.gameSessions.isEmpty() }
            .forEach { pool.sessions.remove(it.key) }
    }

    fun cleanExpiredNavigators(pool: SessionPool): Map<MessageRef, NavigationState> {
        val referenceTime = LinuxTime.now()

        return pool.navigates
            .filterValues { referenceTime.timestamp > it.expireDate.timestamp }
            .also { pool.navigates - it.keys }
    }

}
