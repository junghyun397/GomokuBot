package interact.commands.entities

import interact.commands.Command
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash

class HelpCommand(override val name: String = "help") : Command {

    override fun buildCommandData(languageContainer: LanguageContainer): CommandData =
        slash(languageContainer.helpCommand(), languageContainer.helpCommandDescription())

    override fun process(user: User, channel: MessageChannel): Result<Unit> {
        TODO("Not yet implemented")
    }

}