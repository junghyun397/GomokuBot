package core.interact

import core.BotContext
import core.assets.Channel
import core.session.entities.ChannelConfig
import kotlin.time.Instant

interface ExecutionContext {

    val bot: BotContext

    val guild: Channel

    val config: ChannelConfig

    val emittedTime: Instant

    val source: String

}
