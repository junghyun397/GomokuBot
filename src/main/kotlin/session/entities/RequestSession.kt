package session.entities

import utility.UserId

data class RequestSession(val ownerId: UserId, val opponentId: UserId)
