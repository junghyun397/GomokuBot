package core.interact.commands

import arrow.core.raise.Effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.ActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

sealed interface Command {

    val name: String

    val responseFlag: ResponseFlag

    // Routers provide the execution dependencies; composed commands share the same context.
    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ): Result<CommandResult>

    companion object {

        const val COMMAND_REVISION: Int = 10

    }

}

data class CommandResult(
    val io: Effect<Nothing, Unit>,
    val events: List<ActionLog>
) {

    constructor(io: Effect<Nothing, Unit>, event: ActionLog) : this(io, listOf(event))

}
