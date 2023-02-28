package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.flatMap
import utils.structs.map

class LangCommand(private val language: Language) : Command {

    override val name = "lang"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateGuildConfig(bot.sessions, guild, thenConfig)

        val io = service.buildLanguageUpdated(publishers.plain, language.container)
            .launch()
            .flatMap { buildHelpProcedure(bot, thenConfig, publishers.plain, service) }
            .map { listOf(Order.UpsertCommands(thenConfig.language.container)) }

        tuple(io, this.writeCommandReport("set language ${config.language.name} to ${thenConfig.language.name}", guild, user))
    }

}
