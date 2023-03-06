package core.session

import core.assets.GuildUid
import core.assets.MessageRef
import core.database.DatabaseConnection
import core.session.entities.GuildSession
import core.session.entities.MessageBufferKey
import core.session.entities.NavigationState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class SessionPool(
    val sessions: MutableMap<GuildUid, GuildSession> = ConcurrentHashMap(),
    val navigates: MutableMap<MessageRef, NavigationState> = Collections.synchronizedMap(WeakHashMap()),
    val messageBuffer: MutableMap<MessageBufferKey, MutableList<MessageRef>> = Collections.synchronizedMap(WeakHashMap()),
    val dbConnection: DatabaseConnection,
)
