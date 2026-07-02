package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.session.entities.ChannelConfig
import kotlin.time.Instant

interface InternalCommand {

    val name: String

    suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ): Result<CommandResult>

}
