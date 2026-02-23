package discord.interact

import core.assets.ChannelId
import core.assets.SubChannelId

data class DiscordConfig(
    val token: String,
    val officialServerId: ChannelId,
    val archiveSubChannelId: SubChannelId,
    val testerRoleId: Long,
)
