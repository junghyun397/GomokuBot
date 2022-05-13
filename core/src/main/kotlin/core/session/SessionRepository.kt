package core.session

import core.assets.GuildId
import core.assets.Message
import core.database.DatabaseConnection
import core.session.entities.GuildSession
import core.session.entities.NavigateState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionRepository(
    val sessions: MutableMap<GuildId, GuildSession> = ConcurrentHashMap(),
    val navigates: MutableMap<Message, NavigateState> = Collections.synchronizedMap(WeakHashMap()),
    val messageBuffer: MutableMap<String, MutableList<Message>> = Collections.synchronizedMap(WeakHashMap()),
    val databaseConnection: DatabaseConnection,
)
