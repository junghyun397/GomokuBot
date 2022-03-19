package session.entities

import utility.LinuxTime
import utility.UserId

data class RequestSession(val ownerId: UserId, val opponentId: UserId, override val expireDate: LinuxTime) : Expirable
