package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.ActionLog
import core.session.entities.ChannelConfig
import utils.Quadruple
import kotlin.time.Instant

abstract class UnionCommand(private val command: Command) : Command {

    override val responseFlag = this.command.responseFlag

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val (unionIO, unionReport, thenChannel, thenUser) = this.executeSelf(
            bot,
            config,
            channel,
            user,
            service,
            publishers,
            emittedTime
        )
            .getOrThrow()

        val result = this.command.execute(bot, config, thenChannel, thenUser, service, publishers, emittedTime)
            .getOrThrow()

        val io = effect {
            unionIO()
            result.io()
        }

        CommandResult(io, listOf(unionReport) + result.events)
    }

    protected abstract suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ): Result<Quadruple<Effect<Nothing, Unit>, ActionLog, Channel, User.Human>>

}
