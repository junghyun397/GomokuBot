package core.interact.commands

import arrow.core.raise.Effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.Report
import core.session.entities.ChannelConfig

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
    ): CommandResult

}

typealias CommandResult = Result<Pair<Effect<Nothing, Unit>, Report>>
