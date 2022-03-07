package session.entities

import utility.UserId

data class GameSession(val ownerId: UserId, val opponent: UserId)
