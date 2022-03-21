package session.entities

import utility.LinuxTime
import utility.UserId

data class GameSession(val ownerId: UserId, val opponent: UserId?, override val expireDate: LinuxTime) : Expirable
