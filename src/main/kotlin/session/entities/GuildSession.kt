package session.entities

import interact.i18n.LanguageContainer
import interact.i18n.LanguageENG
import utility.UserId

data class GuildSession(
    val languageContainer: LanguageContainer = LanguageENG(),
    val guildConfig: GuildConfig = GuildConfig(),
    val gameSessions: Map<UserId, GameSession> = mapOf(),
    val requestSessions: Map<UserId, RequestSession> = mapOf()
)
