package core.session.entities

import core.assets.Channel
import core.assets.UserUid

data class ChannelSession(
    val guild: Channel,
    val config: ChannelConfig,
    val gameSessions: Map<UserUid, GameSession> = emptyMap(),
    val requestSessions: Map<UserUid, RequestSession> = emptyMap()
)
