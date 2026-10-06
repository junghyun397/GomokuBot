package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.database.DatabaseConnection
import core.database.repositories.ChannelProfileRepository
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class ChannelJoinCommand(private val localeComment: String) : InternalCommand {

    override val name = "channel-join"

    context(dbConnection: DatabaseConnection, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        SessionManager.updateChannelConfig(channel, config)

        val io = effect {
            if (publisher != null)
                buildCombinedHelpProcedure(
                    config = config,
                    publisher = publisher.plain,
                    settingsPage = 0
                )()

            service.upsertCommands(config.language.container)

            ChannelProfileRepository.upsertChannel(
                channel.copy(commandRevision = Command.COMMAND_REVISION)
            )
        }

        CommandResult(io, this.writeActionLog(emittedTime, this.localeComment, channel))
    }

}
