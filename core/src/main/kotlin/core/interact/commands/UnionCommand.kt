package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.InteractionReport
import core.session.entities.ChannelConfig
import utils.lang.tuple
import utils.structs.Quadruple

abstract class UnionCommand(private val command: Command) : Command {

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val (unionIO, unionReport, thenChannel, thenUser) = this.executeSelf(bot, config, guild, user, service, messageRef, publishers)
            .getOrThrow()

        val (originalIO, report) = this.command.execute(bot, config, thenChannel, thenUser, service, messageRef, publishers)
            .getOrThrow()

        val io = effect {
            val unionOrders = unionIO()
            val originalOrders = originalIO()
            unionOrders + originalOrders
        }

        tuple(io, unionReport + report)
    }

    protected abstract suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ): Result<Quadruple<Effect<Nothing, List<Order>>, InteractionReport, Channel, User>>

}
