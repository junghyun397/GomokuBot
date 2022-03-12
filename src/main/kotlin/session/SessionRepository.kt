package session

import database.DatabaseConnection
import session.entities.GuildSession

class SessionRepository(
    val sessions: MutableMap<Long, GuildSession> = mutableMapOf(),
    val databaseConnection: DatabaseConnection
)
