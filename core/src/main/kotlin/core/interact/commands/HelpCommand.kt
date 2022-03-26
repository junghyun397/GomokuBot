package core.interact.commands

import core.BotContext
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.monads.IO
import utils.values.UserId

class HelpCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messageBinder: MessageBinder<A, B>,
        messagePublisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Unit>, CommandReport>> = runCatching {
        val io = IO { }
            .flatMap { messageBinder.bindAboutBot(guildConfig.language.container, messagePublisher) }
            .flatMap { messageBinder.bindCommandGuide(guildConfig.language.container, messagePublisher) }
            .flatMap { messageBinder.bindStyleGuide(guildConfig.language.container, messagePublisher) }
            .flatMap { messageBinder.bindLanguageGuide(messagePublisher) }

        io to this.asCommandReport("succeed")
    }

}
