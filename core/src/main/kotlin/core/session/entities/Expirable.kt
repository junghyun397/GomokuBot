package core.session.entities

import utils.assets.LinuxTime

sealed interface Expirable {

    val expireDate: LinuxTime

}
