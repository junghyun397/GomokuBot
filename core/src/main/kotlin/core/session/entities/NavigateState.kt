package core.session.entities

import utils.assets.LinuxTime

enum class NavigableKind {
    BOARD, HELP, SETTINGS, ABOUT_RENJU
}

data class NavigateState(val navigableKind: NavigableKind, val page: Int, override val expireDate: LinuxTime): Expirable
