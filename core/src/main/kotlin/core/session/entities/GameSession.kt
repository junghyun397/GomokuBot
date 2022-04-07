package core.session.entities

import core.assets.Message
import core.assets.User
import jrenju.L3Board
import jrenju.notation.Pos
import utils.structs.Option
import utils.assets.LinuxTime

data class GameSession(
    val owner: User, val opponent: Option<User>,
    val ownerHasBlack: Boolean,
    val board: L3Board,
    val history: List<Pos>, val messages: List<Message>,
    override val expireDate: LinuxTime,
) : Expirable
