package interact.commands

import interact.commands.entities.HelpCommand
import interact.commands.entities.StartCommand
import interact.commands.entities.StyleCommand
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Collection<BuildableCommand> =
    listOf(HelpCommand, StartCommand, StyleCommand)

interface BuildableCommand {

    fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer): CommandListUpdateAction

}
