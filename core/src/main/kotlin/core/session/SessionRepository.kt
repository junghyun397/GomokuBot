package core.session

import core.session.entities.GuildSession
import kotlinx.coroutines.sync.Mutex
import core.assets.GuildId

class SessionRepository(
    val sessions: MutableMap<GuildId, GuildSession> = mutableMapOf(),
    val sessionsMutex: Mutex = Mutex(),
    val databaseConnection: core.database.DatabaseConnection
)
