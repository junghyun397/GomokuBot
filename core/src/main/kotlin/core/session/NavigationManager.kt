package core.session

import core.assets.MessageRef
import core.session.entities.NavigationState
import kotlin.time.Clock

object NavigationManager {

    fun addNavigation(pool: SessionPool, messageRef: MessageRef, state: NavigationState) {
        pool.navigates[messageRef] = state
    }

    fun getNavigationState(pool: SessionPool, messageRef: MessageRef): NavigationState? =
        pool.navigates[messageRef]?.takeIf { Clock.System.now() <= it.expireDate }

    fun cleanExpiredNavigators(pool: SessionPool): Map<MessageRef, NavigationState> {
        val referenceTime = Clock.System.now()

        return pool.navigates
            .filterValues { referenceTime > it.expireDate }
            .onEach { (ref, state) -> pool.navigates.remove(ref, state) }
    }

}
