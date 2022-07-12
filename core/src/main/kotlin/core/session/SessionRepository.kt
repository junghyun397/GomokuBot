package core.session

import core.assets.GuildUid
import core.assets.MessageRef
import core.database.DatabaseConnection
import core.session.entities.GuildSession
import core.session.entities.NavigateState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class SessionRepository(
    val sessions: MutableMap<GuildUid, GuildSession> = ConcurrentHashMap(),
    val navigates: MutableMap<MessageRef, NavigateState> = Collections.synchronizedMap(WeakHashMap()),
    val messageBuffer: MutableMap<String, MutableList<MessageRef>> = Collections.synchronizedMap(WeakHashMap()),
    val dbConnection: DatabaseConnection,
)
