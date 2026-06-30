package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.InteractionReport
import core.session.entities.ChannelConfig
import utils.Quadruple
import utils.tuple

abstract class UnionCommand(private val command: Command) : Command {

    override val responseFlag = this.command.responseFlag

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet
    ) = runCatching {
        val (unionIO, unionReport, thenChannel, thenUser) = this.executeSelf(bot, config, channel, user, service, publishers)
            .getOrThrow()

        val (originalIO, report) = this.command.execute(bot, config, thenChannel, thenUser, service, publishers)
            .getOrThrow()

        val io = effect {
            unionIO()
            originalIO()
        }

        tuple(io, unionReport + report)
    }

    protected abstract suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet
    ): Result<Quadruple<Effect<Nothing, Unit>, InteractionReport, Channel, User.Human>>

}
