package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.MessageRef
import core.assets.UserUid
import core.database.repositories.ChannelConfigRepository
import core.session.entities.*
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.Quadruple

object SessionManager {

    private suspend fun retrieveChannelSession(pool: SessionPool, guild: Channel): ChannelSession =
        pool.sessions.getOrElse(guild.id) {
            ChannelConfigRepository.fetchChannelConfig(pool.dbConnection, guild.id)
                .fold(
                    ifSome = { ChannelSession(guild = guild, config = it) },
                    ifEmpty = { ChannelSession(guild = guild, ChannelConfig()) }
                )
                .also {
                    pool.sessions[guild.id] = it
                }
        }

    private suspend inline fun mapChannelSession(pool: SessionPool, guild: Channel, mapper: (ChannelSession) -> ChannelSession): ChannelSession =
        mapper(this.retrieveChannelSession(pool, guild)).also {
            pool.sessions[guild.id] = it
        }

    suspend fun retrieveChannelConfig(pool: SessionPool, guild: Channel): ChannelConfig =
        this.retrieveChannelSession(pool, guild).config

    suspend fun updateChannelConfig(pool: SessionPool, guild: Channel, channelConfig: ChannelConfig) {
        this.mapChannelSession(pool, guild) {
            it.copy(config = channelConfig)
        }.also {
            ChannelConfigRepository.upsertChannelConfig(pool.dbConnection, guild.id, it.config)
        }
    }

    suspend fun hasRequestSession(pool: SessionPool, guild: Channel, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveChannelSession(pool, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveRequestSession(pool: SessionPool, guild: Channel, userUid: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, guild).requestSessions
            .values
            .firstOrNull { it.owner.id == userUid || it.opponent.id == userUid }

    suspend fun retrieveRequestSessionByOwner(pool: SessionPool, guild: Channel, ownerId: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, guild).requestSessions[ownerId]

    suspend fun retrieveRequestSessionByOpponent(pool: SessionPool, guild: Channel, opponentId: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, guild).requestSessions.values.firstOrNull { it.opponent.id == opponentId }

    suspend fun putRequestSession(pool: SessionPool, guild: Channel, requestSession: RequestSession) {
        this.mapChannelSession(pool, guild) {
            it.copy(requestSessions = it.requestSessions + (requestSession.owner.id to requestSession))
        }
    }

    suspend fun removeRequestSession(pool: SessionPool, guild: Channel, ownerId: UserUid) {
        this.mapChannelSession(pool, guild) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun hasGameSession(pool: SessionPool, guild: Channel, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveChannelSession(pool, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == user1 || it.owner.id == user2 || it.opponent.id == user1 || it.opponent.id == user2 } != null

    suspend fun retrieveGameSession(pool: SessionPool, guild: Channel, userUid: UserUid): GameSession? =
        this.retrieveChannelSession(pool, guild).gameSessions
            .values
            .firstOrNull { it.owner.id == userUid || it.opponent.id == userUid }

    suspend fun putGameSession(pool: SessionPool, guild: Channel, gameSession: GameSession) {
        this.mapChannelSession(pool, guild) {
            it.copy(gameSessions = it.gameSessions + (gameSession.owner.id to gameSession))
        }
    }

    suspend fun removeGameSession(pool: SessionPool, guild: Channel, ownerId: UserUid) {
        this.mapChannelSession(pool, guild) {
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
        crossinline extract: (ChannelSession) -> Map<UserUid, T>,
        crossinline mutate: (ChannelSession, Set<UserUid>) -> ChannelSession
    ): Sequence<Quadruple<ChannelUid, ChannelSession, UserUid, T>> {
        val referenceTime = LinuxTime.now()

        return pool.sessions
            .asSequence()
            .flatMap { (channelId, session) ->
                extract(session)
                    .filter { referenceTime.timestamp > it.value.expireDate.timestamp }
                    .map { (userId, expired) -> tuple(channelId, session, userId, expired) }
                    .also { expires ->
                        pool.sessions[channelId] = mutate(pool.sessions[channelId]!!, expires.map { it.third }.toSet())
                    }
            }
    }

    fun cleanExpiredRequestSessions(pool: SessionPool): Sequence<Quadruple<ChannelUid, ChannelSession, UserUid, RequestSession>> =
        this.cleanExpired(pool,
            extract = { it.requestSessions },
            mutate = { original, exclude -> original.copy(requestSessions = original.requestSessions - exclude) }
        )

    fun cleanExpiredGameSession(pool: SessionPool): Sequence<Quadruple<ChannelUid, ChannelSession, UserUid, GameSession>> =
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
