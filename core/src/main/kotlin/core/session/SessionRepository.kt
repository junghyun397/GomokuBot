package core.session

import core.assets.GuildId
import core.assets.Message
import core.database.DatabaseConnection
import core.session.entities.GuildSession
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionRepository(
    val sessions: MutableMap<GuildId, GuildSession> = ConcurrentHashMap(),
    val messageBuffer: MutableMap<String, MutableList<Message>> = Collections.synchronizedMap(WeakHashMap()),
    val databaseConnection: DatabaseConnection,
)
