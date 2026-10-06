package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.ActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import utils.Quadruple
import kotlin.time.Instant

abstract class UnionCommand(private val command: Command) : Command {

    override val responseFlag = this.command.responseFlag

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val (unionIO, unionReport, thenChannel, thenUser) = this.executeSelf(
            config,
            channel,
            user,
            publishers,
            emittedTime
        )
            .getOrThrow()

        val result = this.command.execute(config, thenChannel, thenUser, publishers, emittedTime)
            .getOrThrow()

        val io = effect {
            unionIO.bind()
            result.io.bind()
        }

        CommandResult(io, listOf(unionReport) + result.events)
    }

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    protected abstract suspend fun executeSelf(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ): Result<Quadruple<Effect<Nothing, Unit>, ActionLog, Channel, User.Human>>

}
