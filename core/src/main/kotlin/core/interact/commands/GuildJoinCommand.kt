package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class GuildJoinCommand(private val localeComment: String) : InternalCommand {

    override val name = "guild-join"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        producer: MessageProducer<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, config)

        val helpProcedure = buildCombinedHelpProcedure(
            bot = bot,
            config = config,
            publisher = publisher.plain,
            producer = producer,
            settingsPage = 0
        )

        val io = helpProcedure.map { listOf(Order.UpsertCommands(config.language.container)) }

        tuple(io, this.writeCommandReport(localeComment, guild))
    }

}
