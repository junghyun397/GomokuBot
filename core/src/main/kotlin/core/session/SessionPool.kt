package core.session

import core.assets.Channel
import core.assets.ChannelUid
import core.assets.MessageRef
import core.assets.UserUid
import core.session.entities.*
import java.util.concurrent.ConcurrentHashMap

data class SessionUserKey(
    val channelId: ChannelUid,
    val userId: UserUid,
)

data class SessionPool(
    internal val channels: MutableMap<ChannelUid, Channel> = ConcurrentHashMap(),
    internal val gameSessions: ConcurrentHashMap<SessionId, SessionSlot<GameSession>> = ConcurrentHashMap(),
    internal val requestSessions: ConcurrentHashMap<SessionId, SessionSlot<RequestSession>> = ConcurrentHashMap(),
    internal val gameSessionIndex: ConcurrentHashMap<SessionUserKey, SessionId> = ConcurrentHashMap(),
    internal val requestSessionIndex: ConcurrentHashMap<SessionUserKey, SessionId> = ConcurrentHashMap(),
    internal val navigates: ConcurrentHashMap<MessageRef, NavigationState> = ConcurrentHashMap(),
)
