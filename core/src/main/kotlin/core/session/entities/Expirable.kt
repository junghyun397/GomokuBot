package core.session.entities

import utils.values.LinuxTime

sealed interface Expirable {

    val expireDate: LinuxTime

}
