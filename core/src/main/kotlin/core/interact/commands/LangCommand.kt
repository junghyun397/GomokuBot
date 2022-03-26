package core.interact.commands

import core.interact.i18n.Language
import core.interact.reports.asCommandReport
import core.BotContext
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.monads.IO
import utils.values.UserId

class LangCommand(override val command: String, private val language: Language) : Command {

    override suspend fun <A, B> execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messageBinder: MessageBinder<A, B>,
        messagePublisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Unit>, CommandReport>> = runCatching {
        IO { } to this.asCommandReport("${guildConfig.language.name} to ${language.name}")
    }

}