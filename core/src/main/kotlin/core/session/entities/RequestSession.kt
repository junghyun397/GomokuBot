package core.session.entities

import core.assets.User
import utils.values.LinuxTime
import core.assets.UserId

data class RequestSession(val owner: User, val opponent: User, override val expireDate: LinuxTime) : Expirable
