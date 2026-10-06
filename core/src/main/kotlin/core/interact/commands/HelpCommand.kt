package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class HelpCommand(
    private val sendSettings: Boolean,
    private val page: Int,
) : Command {

    override val name = "help"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = effect {
            when (this@HelpCommand.sendSettings) {
                true -> buildCombinedHelpProcedure(config, publishers.plain, this@HelpCommand.page)
                else -> buildHelpProcedure(config, publishers.plain, this@HelpCommand.page)
            }()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
