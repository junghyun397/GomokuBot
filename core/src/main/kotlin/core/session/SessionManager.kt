package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.UserUid
import core.database.repositories.ChannelConfigRepository
import core.session.entities.*
import utils.Quadruple
import utils.tuple
import kotlin.time.Clock

class SessionAlreadyExistsException(
    channelId: ChannelUid,
    userId: UserUid?,
) : IllegalStateException("session already exists for $userId in $channelId")

class GameSessionNotFoundException(
    val sessionId: SessionId,
) : IllegalStateException("game session ${sessionId.uuid} not found")

class RequestSessionNotFoundException(
    val sessionId: SessionId,
) : IllegalStateException("request session ${sessionId.uuid} not found")

object SessionManager {

    suspend fun retrieveChannelConfig(pool: SessionPool, channel: Channel): ChannelConfig =
        ChannelConfigRepository.retrieveChannelConfig(pool.dbConnection, channel.id)

    suspend fun updateChannelConfig(pool: SessionPool, channel: Channel, channelConfig: ChannelConfig) {
        ChannelConfigRepository.upsertChannelConfig(pool.dbConnection, channel.id, channelConfig)
    }

    private fun <T : Expirable> insertSession(
        pool: SessionPool,
        channel: Channel,
        sessionId: SessionId,
        participants: Set<UserUid>,
        session: T,
        sessions: MutableMap<SessionId, SessionSlot<T>>,
        indexes: MutableMap<SessionUserKey, SessionId>,
    ) {
        synchronized(pool) {
            val sessionUserKeys = participants.map { tuple(it, SessionUserKey(channel.id, it)) }

            sessionUserKeys.forEach { (userId, key) ->
                if (indexes.containsKey(key))
                    throw SessionAlreadyExistsException(channel.id, userId)
            }

            if (sessions.containsKey(sessionId))
                throw IllegalStateException("session id duplicated")

            pool.channels[channel.id] = channel
            sessions[sessionId] = SessionSlot(session, channel.id, sessionId)
            sessionUserKeys.forEach { (_, key) -> indexes[key] = sessionId }
        }
    }

    fun insertGameSession(
        pool: SessionPool,
        channel: Channel,
        session: GameSession,
    ) {
        this.insertSession(
            pool = pool,
            channel = channel,
            sessionId = session.id,
            participants = setOfNotNull(session.users.black.id, session.users.white.id),
            session = session,
            sessions = pool.gameSessions,
            indexes = pool.gameSessionIndex,
        )
    }

    fun createRequestSession(
        pool: SessionPool,
        channel: Channel,
        participants: Set<UserUid>,
        session: RequestSession,
    ) {
        this.insertSession(
            pool = pool,
            channel = channel,
            sessionId = session.id,
            participants = participants,
            session = session,
            sessions = pool.requestSessions,
            indexes = pool.requestSessionIndex,
        )
    }

    private fun <T : Expirable> deleteSession(
        pool: SessionPool,
        sessions: MutableMap<SessionId, SessionSlot<T>>,
        indexes: MutableMap<SessionUserKey, SessionId>,
        sessionId: SessionId,
    ): T? =
        synchronized(pool) {
            val slot = sessions[sessionId]
            val session = slot?.snapshot()

            if (session != null) {
                sessions.remove(sessionId)
                indexes.entries.removeIf { it.key.channelId == slot.channelId && it.value == sessionId }
                this.removeChannelIfUnused(pool, slot.channelId)

                return@synchronized session
            }

            null
        }

    fun deleteGameSession(pool: SessionPool, sessionId: SessionId): GameSession? =
        this.deleteSession(
            pool = pool,
            sessions = pool.gameSessions,
            indexes = pool.gameSessionIndex,
            sessionId = sessionId,
        )

    fun deleteRequestSession(pool: SessionPool, sessionId: SessionId): RequestSession? =
        this.deleteSession(
            pool = pool,
            sessions = pool.requestSessions,
            indexes = pool.requestSessionIndex,
            sessionId = sessionId,
        )

    fun findGameSessionId(pool: SessionPool, channelUid: ChannelUid, userUid: UserUid): SessionId? =
        pool.gameSessionIndex[SessionUserKey(channelUid, userUid)]

    fun findRequestSessionId(pool: SessionPool, channelUid: ChannelUid, userUid: UserUid): SessionId? =
        pool.requestSessionIndex[SessionUserKey(channelUid, userUid)]

    fun retrieveGameSession(
        pool: SessionPool,
        sessionId: SessionId,
    ): SessionSlot<GameSession> =
        pool.gameSessions[sessionId]
            ?: throw GameSessionNotFoundException(sessionId)

    fun retrieveRequestSession(
        pool: SessionPool,
        sessionId: SessionId,
    ): SessionSlot<RequestSession> =
        pool.requestSessions[sessionId]
            ?: throw RequestSessionNotFoundException(sessionId)

    fun cleanExpiredRequestSessions(pool: SessionPool): Sequence<Quadruple<ChannelUid, Channel, SessionId, RequestSession>> =
        this.cleanExpired(
            pool = pool,
            sessions = pool.requestSessions,
            delete = { sessionId -> this.deleteRequestSession(pool, sessionId) },
        )

    fun cleanExpiredGameSession(pool: SessionPool): Sequence<Quadruple<ChannelUid, Channel, SessionId, GameSession>> =
        this.cleanExpired(
            pool = pool,
            sessions = pool.gameSessions,
            delete = { sessionId -> this.deleteGameSession(pool, sessionId) },
        )

    private fun removeChannelIfUnused(pool: SessionPool, channelId: ChannelUid) {
        if (
            pool.requestSessions.values.none { it.channelId == channelId } &&
            pool.gameSessions.values.none { it.channelId == channelId }
        ) {
            pool.channels.remove(channelId)
        }
    }

    private fun <T : Expirable> cleanExpired(
        pool: SessionPool,
        sessions: Map<SessionId, SessionSlot<T>>,
        delete: (SessionId) -> Unit,
    ): Sequence<Quadruple<ChannelUid, Channel, SessionId, T>> {
        val referenceTime = Clock.System.now()
        val expires = sessions
            .mapNotNull { (sessionId, slot) ->
                val channelId = slot.channelId
                val channel = pool.channels[channelId] ?: return@mapNotNull null
                val session = try {
                    slot.snapshot()
                } catch (_: SessionLockedException) {
                    return@mapNotNull null
                }

                if (referenceTime > session.expireDate)
                    tuple(channelId, channel, sessionId, session)
                else null
            }

        expires.forEach { (_, _, sessionId, _) ->
            runCatching {
                delete(sessionId)
            }
        }

        return expires.asSequence()
    }

}
