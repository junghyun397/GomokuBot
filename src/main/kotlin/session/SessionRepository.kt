package session

import database.DatabaseConnection
import kotlinx.coroutines.sync.Mutex
import session.entities.GuildSession

class SessionRepository(
    val sessions: MutableMap<Long, GuildSession> = mutableMapOf(),
    val sessionsMutex: Mutex = Mutex(),
    val databaseConnection: DatabaseConnection
)
