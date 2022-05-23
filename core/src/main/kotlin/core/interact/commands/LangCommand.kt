package core.interact.commands

import core.BotContext
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

class LangCommand(override val command: String, private val language: Language) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateGuildConfig(bot.sessionRepository, config.id, thenConfig)

        val io = producer.produceLanguageUpdated(publisher, this.language.container)
            .flatMap { helpSequenceAboutBot(bot, thenConfig, producer, publisher) }
            .map { Order.UpsertCommands(thenConfig.language.container) }

        io to this.asCommandReport("${config.language.name} to ${thenConfig.language.name}", user)
    }

}
