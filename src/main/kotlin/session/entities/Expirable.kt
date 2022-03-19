package session.entities

import utility.LinuxTime

sealed interface Expirable {

    val expireDate: LinuxTime

}
