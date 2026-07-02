package core.interact.commands

import arrow.core.raise.Effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.ActionLog
import core.session.entities.ChannelConfig
import kotlin.time.Instant

sealed interface Command {

    val name: String

    val responseFlag: ResponseFlag

    suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ): Result<CommandResult>

}

data class CommandResult(
    val io: Effect<Nothing, Unit>,
    val events: List<ActionLog>
) {

    constructor(io: Effect<Nothing, Unit>, event: ActionLog) : this(io, listOf(event))

}
