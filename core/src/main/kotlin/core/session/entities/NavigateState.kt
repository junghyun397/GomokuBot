package core.session.entities

import utils.assets.LinuxTime

enum class NavigationKind(val range: IntRange) {
    BOARD(0 .. 3), SETTINGS(0 .. 4), ABOUT(0 .. 14)
}

data class NavigateState(val navigationKind: NavigationKind, val page: Int, override val expireDate: LinuxTime): Expirable
