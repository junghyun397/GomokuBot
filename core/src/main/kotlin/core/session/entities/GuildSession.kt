package core.session.entities

import core.interact.i18n.Language
import core.interact.message.graphics.BoardStyle
import utils.values.UserId

data class GuildSession(
    val guildConfig: GuildConfig = GuildConfig(Language.ENG, BoardStyle.IMAGE),
    val gameSessions: Map<UserId, GameSession> = mapOf(),
    val requestSessions: Map<UserId, RequestSession> = mapOf()
)
