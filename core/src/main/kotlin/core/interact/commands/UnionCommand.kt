package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.AbstractInteractionReport
import core.session.entities.GuildConfig
import utils.lang.pair
import utils.structs.IO
import utils.structs.Quadruple
import utils.structs.flatMap

abstract class UnionCommand(protected val command: Command) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val (unionIO, unionReport, thenGuild, thenUser) = this.executeSelf(bot, config, guild, user, producer, messageRef, publishers)
            .getOrThrow()

        val (originalIO, report) = this.command.execute(bot, config, thenGuild, thenUser, producer, messageRef, publishers)
            .getOrThrow()

        unionIO.flatMap { originalIO } pair unionReport + report
    }

    protected abstract suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ): Result<Quadruple<IO<List<Order>>, AbstractInteractionReport, Guild, User>>

}
