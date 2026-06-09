package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.MessageRef
import core.assets.UserUid
import core.database.DatabaseConnection
import core.session.entities.ChannelConfig
import core.session.entities.GameSession
import core.session.entities.MessageBufferKey
import core.session.entities.NavigationState
import core.session.entities.RequestSession
import core.session.entities.SessionId
import core.session.entities.SessionSlot
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class SessionUserKey(
    val channelId: ChannelUid,
    val userId: UserUid,
)

data class SessionPool(
    val channelConfigs: MutableMap<ChannelUid, ChannelConfig> = ConcurrentHashMap(),
    val channels: MutableMap<ChannelUid, Channel> = ConcurrentHashMap(),
    val gameSessions: ConcurrentHashMap<SessionId, SessionSlot<GameSession>> = ConcurrentHashMap(),
    val requestSessions: ConcurrentHashMap<SessionId, SessionSlot<RequestSession>> = ConcurrentHashMap(),
    val gameSessionIndex: ConcurrentHashMap<SessionUserKey, SessionId> = ConcurrentHashMap(),
    val requestSessionIndex: ConcurrentHashMap<SessionUserKey, SessionId> = ConcurrentHashMap(),
    val navigates: MutableMap<MessageRef, NavigationState> = Collections.synchronizedMap(WeakHashMap()),
    val messageBuffer: MutableMap<MessageBufferKey, MutableList<MessageRef>> = Collections.synchronizedMap(WeakHashMap()),
    val dbConnection: DatabaseConnection,
)
