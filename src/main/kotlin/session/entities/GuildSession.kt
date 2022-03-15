package session.entities

import interact.i18n.Language
import interact.message.graphics.Style
import utility.UserId

data class GuildSession(
    val guildConfig: GuildConfig = GuildConfig(Language.ENG, Style.IMAGE),
    val gameSessions: Map<UserId, GameSession> = mapOf(),
    val requestSessions: Map<UserId, RequestSession> = mapOf()
)
