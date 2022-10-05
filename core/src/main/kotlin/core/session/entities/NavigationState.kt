package core.session.entities

import core.assets.*
import core.inference.FocusSolver
import renju.notation.Renju
import utils.assets.LinuxTime
import utils.structs.Identifiable

enum class NavigationKind(override val id: Short, val range: IntRange, val emojis: Set<String>) : Identifiable {

    BOARD(0, 0 until Renju.BOARD_SIZE(), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    SETTINGS(1, 0 .. 5, setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ABOUT(2, 0 .. 14, setOf(UNICODE_LEFT, UNICODE_RIGHT));

    companion object {

        val emojis: Set<String> = NavigationKind.values()
            .map { it.emojis }
            .reduce { acc, kind -> acc + kind }

    }

}

sealed interface NavigationState : Expirable {

    val kind: NavigationKind

    val page: Int

}

data class PageNavigationState(
    private val messageRef: MessageRef,
    override val kind: NavigationKind,
    override val page: Int,
    override val expireDate: LinuxTime,
) : NavigationState

data class BoardNavigationState(
    override val page: Int,
    val focusInfo: FocusSolver.FocusInfo,
    override val expireDate: LinuxTime,
): NavigationState {

    override val kind = NavigationKind.BOARD

}
