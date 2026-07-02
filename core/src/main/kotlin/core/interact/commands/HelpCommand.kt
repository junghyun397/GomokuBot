package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class HelpCommand(
    private val sendSettings: Boolean,
    private val page: Int,
) : Command {

    override val name = "help"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = effect {
            when (this@HelpCommand.sendSettings) {
                true -> buildCombinedHelpProcedure(bot, config, publishers.plain, service, page)
                else -> buildHelpProcedure(bot, config, publishers.plain, service, page)
            }()
            Unit
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
