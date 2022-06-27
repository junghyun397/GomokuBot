package core.session.entities

import core.assets.Guild
import core.assets.UserUid

data class GuildSession(
    val guild: Guild,
    val config: GuildConfig,
    val gameSessions: Map<UserUid, GameSession> = emptyMap(),
    val requestSessions: Map<UserUid, RequestSession> = emptyMap()
)
