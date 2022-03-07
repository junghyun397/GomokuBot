package interact.commands.entities

import interact.commands.CommandReport
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import utility.MessagePublisher

interface Command {

    val name: String

    fun buildCommandData(languageContainer: LanguageContainer): CommandData

    suspend fun execute(user: User, messagePublisher: MessagePublisher): Result<CommandReport>

}