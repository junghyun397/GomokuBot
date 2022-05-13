package core

import core.assets.ChannelId
import java.time.Duration

data class BotConfig(
    val expireOffset: Long = Duration.ofHours(5).toMillis(),
    val archiveChannelId: ChannelId = ChannelId(553959991489331200),
    val officialChannel: String = "https://discord.gg/vq8pkfF"
)
