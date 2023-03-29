package core.session.entities

import core.assets.User
import core.session.Rule
import utils.assets.LinuxTime

data class RequestSession(
    val owner: User,
    val opponent: User,
    val messageBufferKey: MessageBufferKey,
    val rule: Rule,
    override val expireDate: LinuxTime,
) : Expirable
