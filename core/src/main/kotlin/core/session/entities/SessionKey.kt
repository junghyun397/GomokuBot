package core.session.entities

import core.assets.ChannelId
import core.assets.UserId

data class SessionKey(
    val channelId: ChannelId,
    val userId: UserId,
)
