package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import utils.tuple

class ChannelJoinCommand(private val localeComment: String) : InternalCommand {

    override val name = "channel-join"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
    ) = runCatching {
        SessionManager.updateChannelConfig(bot.sessions, channel, config)

        val io = effect {
            if (publisher != null)
                buildCombinedHelpProcedure(
                    bot = bot,
                    config = config,
                    publisher = publisher.plain,
                    service = service,
                    settingsPage = 0
                )()

            service.upsertCommands(config.language.container)
        }

        tuple(io, this.writeCommandReport(this.localeComment, channel))
    }

}
