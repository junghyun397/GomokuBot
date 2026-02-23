package core.interact

import core.BotContext
import core.assets.Channel
import core.session.entities.ChannelConfig
import utils.assets.LinuxTime

interface ExecutionContext {

    val bot: BotContext

    val guild: Channel

    val config: ChannelConfig

    val emittedTime: LinuxTime

    val source: String

}
