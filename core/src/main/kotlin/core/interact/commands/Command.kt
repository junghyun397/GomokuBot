package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.Report
import core.session.entities.GuildConfig
import utils.structs.IO

sealed interface Command {

    val name: String

    val responseFlag: ResponseFlag

    suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ): CommandResult

}

typealias CommandResult = Result<Pair<IO<List<Order>>, Report>>
