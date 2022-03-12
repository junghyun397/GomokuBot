package interact.commands.entities

import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import route.BotContext
import utility.MessagePublisher
import utility.UserId

interface Command {

    val name: String

    suspend fun execute(
        botContext: BotContext,
        languageContainer: LanguageContainer,
        user: UserId,
        messagePublisher: MessagePublisher,
    ): Result<CommandReport>

}