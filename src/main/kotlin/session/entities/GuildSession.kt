package session.entities

import interact.i18n.LanguageContainer

data class GuildSession(val languageContainer: LanguageContainer, val gameSessions: Collection<GameSession>, val requestSession: RequestSession)