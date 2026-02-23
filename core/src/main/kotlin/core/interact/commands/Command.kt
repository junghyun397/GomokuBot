package core.interact.commands

import arrow.core.raise.Effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.Report
import core.session.entities.ChannelConfig

sealed interface Command {

    val name: String

    val responseFlag: ResponseFlag

    suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ): CommandResult

}

typealias CommandResult = Result<Pair<Effect<Nothing, List<Order>>, Report>>
