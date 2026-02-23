package discord.interact

import core.BotContext
import core.assets.Channel
import core.interact.ExecutionContext
import core.session.entities.ChannelConfig
import utils.assets.LinuxTime

data class TaskContext(
    override val bot: BotContext,
    override val guild: Channel,
    override val config: ChannelConfig,
    override val emittedTime: LinuxTime,
    override val source: String
) : ExecutionContext
