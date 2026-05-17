package discord.interact

import core.BotContext
import core.assets.Channel
import core.interact.ExecutionContext
import core.session.entities.ChannelConfig
import kotlin.time.Instant

data class TaskContext(
    override val bot: BotContext,
    override val channel: Channel,
    override val config: ChannelConfig,
    override val emittedTime: Instant,
    override val source: String
) : ExecutionContext
