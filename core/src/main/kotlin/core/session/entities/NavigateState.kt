package core.session.entities

import core.assets.*
import jrenju.notation.Renju
import utils.assets.LinuxTime

enum class NavigationKind(val range: IntRange, val emojis: Set<String>) {
    BOARD(0 until Renju.BOARD_SIZE(), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    SETTINGS(0 .. 4, setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ABOUT(0 .. 14, setOf(UNICODE_LEFT, UNICODE_RIGHT))
}

data class NavigateState(val navigationKind: NavigationKind, val page: Int, override val expireDate: LinuxTime): Expirable
