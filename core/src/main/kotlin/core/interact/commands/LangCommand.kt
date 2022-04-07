package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.interact.i18n.Language
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.structs.IO

class LangCommand(override val command: String, private val language: Language) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(context.sessionRepository, config.id, config.copy(language = language))

        val io = producer.produceLanguageUpdated(publisher, this.language.container)
            .map { it.launch(); Order.RefreshCommands }

        io to this.asCommandReport("${config.language.name} to ${language.name}", user)
    }

}
