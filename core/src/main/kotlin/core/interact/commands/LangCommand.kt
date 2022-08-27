package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

class LangCommand(override val name: String, private val language: Language) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateGuildConfig(bot.sessions, guild, thenConfig)

        val io = producer.produceLanguageUpdated(publisher, this.language.container)
            .flatMap { it.launch() }
            .flatMap { buildHelpSequence(bot, thenConfig, publisher, producer) }
            .map { listOf(Order.UpsertCommands(thenConfig.language.container)) }

        io and this.asCommandReport("${config.language.name} to ${thenConfig.language.name}", user)
    }

}
