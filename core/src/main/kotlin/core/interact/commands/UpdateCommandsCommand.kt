package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.ChannelProfileRepository
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.entities.ChannelConfig
import utils.tuple
import kotlin.time.Instant

class UpdateCommandsCommand(
    command: Command,
    private val previousRevision: Int,
    private val targetRevision: Int,
) : UnionCommand(command) {

    override val name = "update-commands"

    override suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val channel = channel.copy(commandRevision = this.targetRevision)

        val io: Effect<Nothing, Unit> = effect {
            service.upsertCommands(config.language.container)
            ChannelProfileRepository.upsertChannel(bot.dbConnection, channel)
        }

        val report = this.writeActionLog(
            emittedTime,
            "revision ${this.previousRevision} to ${this.targetRevision}",
            channel,
            user
        )

        tuple(io, report, channel, user)
    }

}
