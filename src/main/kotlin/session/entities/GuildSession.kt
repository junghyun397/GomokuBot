package session.entities

import interact.i18n.Language
import interact.message.graphics.BoardStyle
import utility.UserId

data class GuildSession(
    val guildConfig: GuildConfig = GuildConfig(Language.ENG, BoardStyle.IMAGE),
    val gameSessions: Map<UserId, GameSession> = mapOf(),
    val requestSessions: Map<UserId, RequestSession> = mapOf()
)
