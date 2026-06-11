package core.session.entities

import core.assets.ChannelUid
import kotlinx.coroutines.sync.Mutex

class SessionLockedException(
    val sessionId: SessionId,
) : IllegalStateException("session ${sessionId.uuid} is locked")

class SessionSlot<T : Expirable>(
    private var session: T,
    val channelId: ChannelUid,
    private val sessionId: SessionId,
) {

    private val mutex = Mutex()

    fun snapshot(): T {
        if (!this.mutex.tryLock()) throw SessionLockedException(this.sessionId)

        return try {
            this.session
        } finally {
            this.mutex.unlock()
        }
    }

    suspend fun mutate(block: suspend (T) -> T): T {
        if (!this.mutex.tryLock()) throw SessionLockedException(this.sessionId)

        return try {
            this.session = block(this.session)
            this.session
        } finally {
            this.mutex.unlock()
        }
    }

}
