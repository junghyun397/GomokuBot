package core.session.entities

import utils.values.LinuxTime
import utils.values.UserId

data class GameSession(val ownerId: UserId, val opponent: UserId?, override val expireDate: LinuxTime) : Expirable
