package core.session.entities

import core.assets.*
import jrenju.notation.Renju
import utils.assets.LinuxTime

enum class NavigationKind(val range: IntRange, val emojis: Set<String>) {
    BOARD(0 until Renju.BOARD_SIZE(), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    SETTINGS(0 .. 4, setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ABOUT(0 .. 14, setOf(UNICODE_LEFT, UNICODE_RIGHT))
}

sealed interface NavigateState : Expirable {

    val navigateKind: NavigationKind

    val page: Int

}

data class PageNavigateState(
    private val messageRef: MessageRef,
    override val navigateKind: NavigationKind,
    override val page: Int,
    override val expireDate: LinuxTime,
) : NavigateState

data class BoardNavigateState(
    override val page: Int,
    override val expireDate: LinuxTime,
): NavigateState {

    override val navigateKind = NavigationKind.BOARD

}
