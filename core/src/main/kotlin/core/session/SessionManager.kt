package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.UserUid
import core.database.DatabaseConnection
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

    context(connection: DatabaseConnection)
    suspend fun retrieveChannelConfig(channel: Channel): ChannelConfig =
        ChannelConfigRepository.retrieveChannelConfig(channel.id)

    context(connection: DatabaseConnection)
    suspend fun updateChannelConfig(channel: Channel, channelConfig: ChannelConfig) {
        ChannelConfigRepository.upsertChannelConfig(channel.id, channelConfig)
    }

    context(pool: SessionPool)
    private fun <T : Expirable> insertSession(
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

    context(pool: SessionPool)
    fun insertGameSession(
        channel: Channel,
        session: GameSession,
    ) {
        this.insertSession(
            channel = channel,
            sessionId = session.id,
            participants = setOfNotNull(session.users.black.id, session.users.white.id),
            session = session,
            sessions = pool.gameSessions,
            indexes = pool.gameSessionIndex,
        )
    }

    context(pool: SessionPool)
    fun createRequestSession(
        channel: Channel,
        participants: Set<UserUid>,
        session: RequestSession,
    ) {
        this.insertSession(
            channel = channel,
            sessionId = session.id,
            participants = participants,
            session = session,
            sessions = pool.requestSessions,
            indexes = pool.requestSessionIndex,
        )
    }

    context(pool: SessionPool)
    private fun <T : Expirable> removeSession(
        sessions: MutableMap<SessionId, SessionSlot<T>>,
        indexes: MutableMap<SessionUserKey, SessionId>,
        sessionId: SessionId,
    ) {
        synchronized(pool) {
            val slot = sessions.remove(sessionId) ?: return@synchronized
            indexes.entries.removeIf { it.key.channelId == slot.channelId && it.value == sessionId }
            this.removeChannelIfUnused(slot.channelId)
        }
    }

    context(pool: SessionPool)
    fun finishGameSession(runtime: SessionRuntime<GameSession>) {
        runtime.close()
        this.removeSession(pool.gameSessions, pool.gameSessionIndex, runtime.session.id)
    }

    context(pool: SessionPool)
    fun finishRequestSession(runtime: SessionRuntime<RequestSession>) {
        runtime.close()
        this.removeSession(pool.requestSessions, pool.requestSessionIndex, runtime.session.id)
    }

    context(pool: SessionPool)
    fun finishUndoRequest(runtime: SessionRuntime<GameSession>): SessionRuntime<RequestSession>? {
        val request = runtime.undoRequest ?: return null
        runtime.undoRequest = null
        this.finishRequestSession(request)
        return request
    }

    context(pool: SessionPool)
    fun findGameSessionId(channelUid: ChannelUid, userUid: UserUid): SessionId? =
        pool.gameSessionIndex[SessionUserKey(channelUid, userUid)]

    context(pool: SessionPool)
    fun findRequestSessionId(channelUid: ChannelUid, userUid: UserUid): SessionId? =
        pool.requestSessionIndex[SessionUserKey(channelUid, userUid)]

    context(pool: SessionPool)
    fun retrieveGameSession(
        sessionId: SessionId,
    ): SessionSlot<GameSession> =
        pool.gameSessions[sessionId]
            ?: throw GameSessionNotFoundException(sessionId)

    context(pool: SessionPool)
    fun retrieveRequestSession(
        sessionId: SessionId,
    ): SessionSlot<RequestSession> =
        pool.requestSessions[sessionId]
            ?: throw RequestSessionNotFoundException(sessionId)

    context(pool: SessionPool)
    fun cleanExpiredRequestSessions(): Sequence<Quadruple<ChannelUid, Channel, SessionId, SessionRuntime<RequestSession>>> =
        this.cleanExpired(
            sessions = pool.requestSessions,
            finish = { runtime -> this.finishRequestSession(runtime) },
        )

    context(pool: SessionPool)
    fun cleanExpiredGameSession(): Sequence<Quadruple<ChannelUid, Channel, SessionId, SessionRuntime<GameSession>>> =
        this.cleanExpired(
            sessions = pool.gameSessions,
            finish = { runtime -> this.finishGameSession(runtime) },
        )

    context(pool: SessionPool)
    private fun removeChannelIfUnused(channelId: ChannelUid) {
        if (
            pool.requestSessions.values.none { it.channelId == channelId } &&
            pool.gameSessions.values.none { it.channelId == channelId }
        ) {
            pool.channels.remove(channelId)
        }
    }

    context(pool: SessionPool)
    private fun <T : Expirable> cleanExpired(
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
