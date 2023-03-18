package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.InteractionReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO
import utils.structs.Quadruple
import utils.structs.flatMap

abstract class UnionCommand(private val command: Command) : Command {

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val (unionIO, unionReport, thenGuild, thenUser) = this.executeSelf(bot, config, guild, user, service, messageRef, publishers)
            .getOrThrow()

        val (originalIO, report) = this.command.execute(bot, config, thenGuild, thenUser, service, messageRef, publishers)
            .getOrThrow()

        tuple(unionIO.flatMap { originalIO }, unionReport + report)
    }

    protected abstract suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ): Result<Quadruple<IO<List<Order>>, InteractionReport, Guild, User>>

}
