package core.interact.commands

import core.BotContext
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.monads.IO
import utils.values.UserId

sealed interface Command {

    val command: String

    suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        userId: UserId,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Unit>, CommandReport>>

}
