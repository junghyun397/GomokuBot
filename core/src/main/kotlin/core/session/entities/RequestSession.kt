package core.session.entities

import core.assets.User
import core.session.Rule
import kotlin.time.Instant

data class RequestSession(
    val id: SessionId,
    val owner: User.Human,
    val opponent: User.Human,
    val messageBufferKey: MessageBufferKey,
    val rule: Rule,
    override val expireDate: Instant,
) : Expirable
