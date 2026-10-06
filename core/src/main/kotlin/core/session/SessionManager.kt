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

    private fun <T : Expirable> removeSession(
        pool: SessionPool,
        sessions: MutableMap<SessionId, SessionSlot<T>>,
        indexes: MutableMap<SessionUserKey, SessionId>,
        sessionId: SessionId,
    ) {
        synchronized(pool) {
            val slot = sessions.remove(sessionId) ?: return@synchronized
            indexes.entries.removeIf { it.key.channelId == slot.channelId && it.value == sessionId }
            this.removeChannelIfUnused(pool, slot.channelId)
        }
    }

    fun finishGameSession(pool: SessionPool, runtime: SessionRuntime<GameSession>) {
        runtime.close()
        this.removeSession(pool, pool.gameSessions, pool.gameSessionIndex, runtime.session.id)
    }

    fun finishRequestSession(pool: SessionPool, runtime: SessionRuntime<RequestSession>) {
        runtime.close()
        this.removeSession(pool, pool.requestSessions, pool.requestSessionIndex, runtime.session.id)
    }

    fun finishUndoRequest(pool: SessionPool, runtime: SessionRuntime<GameSession>): SessionRuntime<RequestSession>? {
        val request = runtime.undoRequest ?: return null
        runtime.undoRequest = null
        this.finishRequestSession(pool, request)
        return request
    }

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

    fun cleanExpiredRequestSessions(pool: SessionPool): Sequence<Quadruple<ChannelUid, Channel, SessionId, SessionRuntime<RequestSession>>> =
        this.cleanExpired(
            pool = pool,
            sessions = pool.requestSessions,
            finish = { runtime -> this.finishRequestSession(pool, runtime) },
        )

    fun cleanExpiredGameSession(pool: SessionPool): Sequence<Quadruple<ChannelUid, Channel, SessionId, SessionRuntime<GameSession>>> =
        this.cleanExpired(
            pool = pool,
            sessions = pool.gameSessions,
            finish = { runtime -> this.finishGameSession(pool, runtime) },
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
        finish: (SessionRuntime<T>) -> Unit,
    ): Sequence<Quadruple<ChannelUid, Channel, SessionId, SessionRuntime<T>>> {
        val referenceTime = Clock.System.now()

        return sessions.mapNotNull { (sessionId, slot) ->
            val channel = pool.channels[slot.channelId] ?: return@mapNotNull null
            val runtime = slot.closeIfExpired(referenceTime) ?: return@mapNotNull null
            finish(runtime)
            tuple(slot.channelId, channel, sessionId, runtime)
        }.asSequence()
    }

}
