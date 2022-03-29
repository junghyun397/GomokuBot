package core.session.entities

import utils.values.LinuxTime
import utils.values.UserId

data class GameSession(
    val ownerId: UserId, val opponent: UserId?, override val expireDate: LinuxTime,
    val board: Unit, val turns: Int,
) : Expirable

fun RequestSession.asGameSession() =
    GameSession(
        this.ownerId, this.opponentId, LinuxTime(),
        Unit, 0
    )
