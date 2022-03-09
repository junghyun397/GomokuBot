package interact.commands.entities

import BotContext
import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import net.dv8tion.jda.api.entities.User
import utility.GuildId
import utility.MessagePublisher
import utility.UserId

interface Command {

    val name: String

    suspend fun execute(
        botContext: BotContext,
        languageContainer: LanguageContainer,
        user: UserId,
        guild: GuildId,
        messagePublisher: MessagePublisher,
    ): Result<CommandReport>

}