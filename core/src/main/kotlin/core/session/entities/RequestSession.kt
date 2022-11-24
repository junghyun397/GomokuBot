package core.session.entities

import core.assets.User
import utils.assets.LinuxTime

data class RequestSession(
    val owner: User,
    val opponent: User,
    val messageBufferKey: MessageBufferKey,
    override val expireDate: LinuxTime,
) : Expirable
