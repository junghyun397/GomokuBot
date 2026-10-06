package core.session

import core.assets.MessageRef
import core.session.entities.NavigationState
import kotlin.time.Clock

object NavigationManager {

    context(pool: SessionPool)
    fun addNavigation(messageRef: MessageRef, state: NavigationState) {
        pool.navigates[messageRef] = state
    }

    context(pool: SessionPool)
    fun getNavigationState(messageRef: MessageRef): NavigationState? =
        pool.navigates[messageRef]?.takeIf { Clock.System.now() <= it.expireDate }

    context(pool: SessionPool)
    fun cleanExpiredNavigators(): Map<MessageRef, NavigationState> {
        val referenceTime = Clock.System.now()

        return pool.navigates
            .filterValues { referenceTime > it.expireDate }
            .onEach { (ref, state) -> pool.navigates.remove(ref, state) }
    }

}
