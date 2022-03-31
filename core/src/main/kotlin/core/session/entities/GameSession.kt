package core.session.entities

import core.assets.User
import jrenju.DeepL3Board
import jrenju.L3Board
import utils.monads.Option
import utils.values.LinuxTime

data class GameSession(
    val owner: User, val opponent: Option<User>,
    val board: L3Board,
    override val expireDate: LinuxTime,
) : Expirable
