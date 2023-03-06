package discord.interact

import core.BotContext
import core.assets.Guild
import core.interact.ExecutionContext
import core.session.entities.GuildConfig
import utils.assets.LinuxTime

data class TaskContext(
    override val bot: BotContext,
    override val guild: Guild,
    override val config: GuildConfig,
    override val emittedTime: LinuxTime,
    override val source: String
) : ExecutionContext
