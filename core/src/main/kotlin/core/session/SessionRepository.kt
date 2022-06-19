package core.session

import core.assets.GuildId
import core.assets.MessageRef
import core.database.DatabaseConnection
import core.session.entities.GuildSession
import core.session.entities.NavigateState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class SessionRepository(
    val sessions: MutableMap<GuildId, GuildSession> = ConcurrentHashMap(),
    val navigates: MutableMap<MessageRef, NavigateState> = Collections.synchronizedMap(WeakHashMap()),
    val messageBuffer: MutableMap<String, MutableList<MessageRef>> = Collections.synchronizedMap(WeakHashMap()),
    val databaseConnection: DatabaseConnection,
)
