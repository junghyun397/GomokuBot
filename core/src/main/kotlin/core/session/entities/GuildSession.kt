package core.session.entities

import core.assets.UserId

data class GuildSession(
    val guildConfig: GuildConfig,
    val gameSessions: Map<UserId, GameSession> = emptyMap(),
    val requestSessions: Map<UserId, RequestSession> = emptyMap()
)
