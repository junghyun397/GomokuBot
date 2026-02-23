package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple

class GuildJoinCommand(private val localeComment: String) : InternalCommand {

    override val name = "guild-join"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, config)

        val helpProcedure = buildCombinedHelpProcedure(
            bot = bot,
            config = config,
            publisher = publisher.plain,
            service = service,
            settingsPage = 0
        )

        val io = effect {
            helpProcedure()
            listOf(Order.UpsertCommands(config.language.container))
        }

        tuple(io, this.writeCommandReport(localeComment, guild))
    }

}
