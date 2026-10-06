package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.database.DatabaseConnection
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

object ChannelLeaveCommand : InternalCommand {

    override val name = "channel-leave"

    context(dbConnection: DatabaseConnection, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        val io: Effect<Nothing, Unit> = effect { }
        CommandResult(io, this.writeActionLog(emittedTime, "goodbye", channel))
    }

}
