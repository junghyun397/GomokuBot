package interact.commands

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface Command {

    val name: String

    fun buildCommandData(languageContainer: LanguageContainer): CommandData

    fun process(user: User, channel: MessageChannel): Result<Unit>

}