package core.interact

import core.assets.Channel
import core.session.entities.ChannelConfig
import kotlin.time.Instant

interface ExecutionContext {

    val channel: Channel

    val config: ChannelConfig

    val emittedTime: Instant

    val source: String

}
