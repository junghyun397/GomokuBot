package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.entities.ChannelConfig
import kotlin.time.Instant

object ChannelLeaveCommand : InternalCommand {

    override val name = "channel-leave"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        val io: Effect<Nothing, Unit> = effect { }
        CommandResult(io, this.writeActionLog(emittedTime, "goodbye", channel))
    }

}
