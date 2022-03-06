package route

import interact.commands.Command
import interact.commands.entities.HelpCommand
import interact.i18n.LanguageContainer

fun mapCommand(command: String, languageContainer: LanguageContainer): Result<Command> =
    when (command) {
        languageContainer.helpCommand() -> Result.success(HelpCommand())
        else -> Result.failure(Exception())
    }
