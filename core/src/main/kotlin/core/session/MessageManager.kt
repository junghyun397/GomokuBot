package core.session

import core.assets.MessageRef
import core.session.entities.MessageBufferKey
import core.session.entities.NavigationState
import kotlin.time.Clock

object MessageManager {

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

    fun cleanExpiredNavigators(pool: SessionPool): Map<MessageRef, NavigationState> {
        val referenceTime = Clock.System.now()

        return pool.navigates
            .filterValues { referenceTime > it.expireDate }
            .also { expired -> expired.keys.forEach { pool.navigates.remove(it) } }
    }

}
