package discord.interact

import core.assets.ChannelId
import core.assets.GuildId

data class DiscordConfig(
    val token: String,
    val officialServerId: GuildId,
    val archiveChannelId: ChannelId,
    val testerRoleId: Long,
)
