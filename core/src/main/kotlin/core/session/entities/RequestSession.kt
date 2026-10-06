package core.session.entities

import core.assets.User
import kotlin.time.Instant

sealed interface RequestSession : Expirable {

    val id: SessionId
    val requester: User.Human
    val recipient: User.Human

    data class Match(
        override val id: SessionId,
        override val requester: User.Human,
        override val recipient: User.Human,
        val rule: Rule,
        override val expireDate: Instant,
    ) : RequestSession

    data class Undo(
        override val id: SessionId,
        override val requester: User.Human,
        override val recipient: User.Human,
        val gameSessionId: SessionId,
        override val expireDate: Instant,
    ) : RequestSession

}
