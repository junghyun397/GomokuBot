package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.tuple

object ChannelLeaveCommand : InternalCommand {

    override val name = "channel-leave"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
    ) = runCatching {
        val io: Effect<Nothing, Unit> = effect { }
        tuple(io, this.writeCommandReport("goodbye", channel))
    }

}
