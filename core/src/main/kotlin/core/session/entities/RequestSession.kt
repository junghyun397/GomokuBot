package core.session.entities

import utils.values.LinuxTime
import utils.values.UserId

data class RequestSession(val ownerId: UserId, val opponentId: UserId, override val expireDate: LinuxTime) : Expirable
