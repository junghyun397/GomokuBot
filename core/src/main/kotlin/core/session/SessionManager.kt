package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.MessageRef
import core.assets.UserUid
import core.assets.humanId
import core.database.repositories.ChannelConfigRepository
import core.session.entities.*
import utils.lang.tuple
import utils.structs.Quadruple
import kotlin.time.Clock

object SessionManager {

    private suspend fun retrieveChannelSession(pool: SessionPool, channel: Channel): ChannelSession =
        pool.sessions.getOrElse(channel.id) {
            ChannelConfigRepository.fetchChannelConfig(pool.dbConnection, channel.id)
                .fold(
                    ifSome = { ChannelSession(channel, config = it) },
                    ifEmpty = { ChannelSession(channel, ChannelConfig()) }
                )
                .also {
                    pool.sessions[channel.id] = it
                }
        }

    private suspend inline fun mapChannelSession(pool: SessionPool, channel: Channel, mapper: (ChannelSession) -> ChannelSession): ChannelSession =
        mapper(this.retrieveChannelSession(pool, channel)).also {
            pool.sessions[channel.id] = it
        }

    suspend fun retrieveChannelConfig(pool: SessionPool, channel: Channel): ChannelConfig =
        this.retrieveChannelSession(pool, channel).config

    suspend fun updateChannelConfig(pool: SessionPool, channel: Channel, channelConfig: ChannelConfig) {
        this.mapChannelSession(pool, channel) {
            it.copy(config = channelConfig)
        }.also {
            ChannelConfigRepository.upsertChannelConfig(pool.dbConnection, channel.id, it.config)
        }
    }

    suspend fun hasRequestSession(pool: SessionPool, channel: Channel, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveChannelSession(pool, channel).requestSessions
            .values
            .firstOrNull { it.owner.humanId == user1 || it.owner.humanId == user2 || it.opponent.humanId == user1 || it.opponent.humanId == user2 } != null

    suspend fun retrieveRequestSession(pool: SessionPool, channel: Channel, userUid: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, channel).requestSessions
            .values
            .firstOrNull { it.owner.humanId == userUid || it.opponent.humanId == userUid }

    suspend fun retrieveRequestSessionByOwner(pool: SessionPool, channel: Channel, ownerId: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, channel).requestSessions[ownerId]

    suspend fun retrieveRequestSessionByOpponent(pool: SessionPool, channel: Channel, opponentId: UserUid): RequestSession? =
        this.retrieveChannelSession(pool, channel).requestSessions.values.firstOrNull { it.opponent.humanId == opponentId }

    suspend fun putRequestSession(pool: SessionPool, hannel: Channel, requestSession: RequestSession) {
        this.mapChannelSession(pool, hannel) {
            it.copy(requestSessions = it.requestSessions + (requestSession.owner.id to requestSession))
        }
    }

    suspend fun removeRequestSession(pool: SessionPool, channel: Channel, ownerId: UserUid) {
        this.mapChannelSession(pool, channel) {
            it.copy(requestSessions = it.requestSessions - ownerId)
        }
    }

    suspend fun hasGameSession(pool: SessionPool, channel: Channel, user1: UserUid, user2: UserUid): Boolean =
        this.retrieveChannelSession(pool, channel).gameSessions
            .values
            .firstOrNull { it.owner.humanId == user1 || it.owner.humanId == user2 || it.opponent.humanId == user1 || it.opponent.humanId == user2 } != null

    suspend fun retrieveGameSession(pool: SessionPool, channel: Channel, userUid: UserUid): GameSession? =
        this.retrieveChannelSession(pool, channel).gameSessions
            .values
            .firstOrNull { it.owner.humanId == userUid || it.opponent.humanId == userUid }

    suspend fun putGameSession(pool: SessionPool, channel: Channel, gameSession: GameSession) {
        this.mapChannelSession(pool, channel) {
            val ownerId = gameSession.owner.humanId ?: throw IllegalStateException()

            it.copy(gameSessions = it.gameSessions + (ownerId to gameSession))
        }
    }

    suspend fun removeGameSession(pool: SessionPool, channel: Channel, ownerId: UserUid) {
        this.mapChannelSession(pool, channel) {
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
        val referenceTime = Clock.System.now()

        return pool.sessions
            .asSequence()
            .flatMap { (channelId, session) ->
                extract(session)
                    .filter { referenceTime > it.value.expireDate }
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
        val referenceTime = Clock.System.now()

        return pool.navigates
            .filterValues { referenceTime > it.expireDate }
            .also { pool.navigates - it.keys }
    }

}
