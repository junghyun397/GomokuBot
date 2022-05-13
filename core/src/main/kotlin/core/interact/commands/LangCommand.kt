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
        SessionManager.updateGuildConfig(bot.sessionRepository, config.id, config.copy(language = language))

        val io = producer.produceLanguageUpdated(publisher, this.language.container)
            .map { it.launch() }
            .flatMap { producer.produceGuideKit(publisher, this.language.container) }
            .map { it.launch(); Order.RefreshCommands(this.language.container) }

        io to this.asCommandReport("${config.language.name} to ${language.name}", user)
    }

}
