package core.interact.commands

import core.assets.Channel
import core.database.DatabaseConnection
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

interface InternalCommand {

    val name: String

    context(dbConnection: DatabaseConnection, sessions: SessionPool, service: PlatformService)
    suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ): Result<CommandResult>

}
