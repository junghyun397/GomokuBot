package core.session.entities

import core.assets.ChannelUid
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Instant

class SessionLockedException(
    val sessionId: SessionId,
) : IllegalStateException("session ${sessionId.uuid} is locked")

class SessionSlot<T : Expirable> internal constructor(
    session: T,
    val channelId: ChannelUid,
    private val sessionId: SessionId,
) {

    private val mutex = Mutex()

    private val runtime = SessionRuntime(session)

    fun snapshot(): T {
        if (!this.mutex.tryLock()) throw SessionLockedException(this.sessionId)

        return try {
            check(!this.runtime.closed) { "session ${this.sessionId.uuid} is closed" }
            this.runtime.session
        } finally {
            this.mutex.unlock()
        }
    }

    internal suspend fun <A> interact(block: suspend (SessionRuntime<T>) -> A): A {
        if (!this.mutex.tryLock()) throw SessionLockedException(this.sessionId)

        return try {
            check(!this.runtime.closed) { "session ${this.sessionId.uuid} is closed" }
            block(this.runtime)
        } finally {
            this.mutex.unlock()
        }
    }

    internal fun closeIfExpired(referenceTime: Instant): SessionRuntime<T>? {
        if (!this.mutex.tryLock()) return null

        return try {
            if (this.runtime.closed || referenceTime <= this.runtime.session.expireDate) null
            else this.runtime.also { it.close() }
        } finally {
            this.mutex.unlock()
        }
    }

}
