package core.session.entities

import core.assets.User
import utils.assets.LinuxTime

data class RequestSession(
    val owner: User,
    val opponent: User,
    val messageBufferKey: String,
    override val expireDate: LinuxTime,
) : Expirable
