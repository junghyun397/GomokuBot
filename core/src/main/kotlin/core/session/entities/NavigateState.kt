package core.session.entities

import jrenju.notation.Renju
import utils.assets.LinuxTime

enum class NavigationKind(val range: IntRange) {
    BOARD(0 until Renju.BOARD_SIZE()), SETTINGS(0 .. 4), ABOUT(0 .. 14)
}

data class NavigateState(val navigationKind: NavigationKind, val page: Int, override val expireDate: LinuxTime): Expirable
