package session

import database.DatabaseConnection
import kotlinx.coroutines.sync.Mutex
import session.entities.GuildSession
import utility.GuildId

class SessionRepository(
    val sessions: MutableMap<GuildId, GuildSession> = mutableMapOf(),
    val sessionsMutex: Mutex = Mutex(),
    val databaseConnection: DatabaseConnection
)
