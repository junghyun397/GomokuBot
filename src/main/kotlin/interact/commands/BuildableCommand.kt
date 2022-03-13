package interact.commands

import interact.commands.entities.HelpCommand
import interact.commands.entities.StartCommand
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.interactions.commands.build.CommandData

val buildableCommand: Collection<BuildableCommand> =
    listOf(HelpCommand, StartCommand)

interface BuildableCommand {

    fun buildCommandData(languageContainer: LanguageContainer): CommandData

}
