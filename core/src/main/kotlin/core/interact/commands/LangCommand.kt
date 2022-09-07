package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

class LangCommand(private val language: Language) : Command {

    override val name = "lang"

    override val responseFlag = ResponseFlag.IMMEDIATELY

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateGuildConfig(bot.sessions, guild, thenConfig)

        val io = producer.produceLanguageUpdated(publishers.plain, this.language.container)
            .launch()
            .flatMap { buildHelpProcedure(bot, thenConfig, publishers.plain, producer) }
            .map { listOf(Order.UpsertCommands(thenConfig.language.container)) }

        io and this.asCommandReport("set language ${config.language.name} to ${thenConfig.language.name}", guild, user)
    }

}
