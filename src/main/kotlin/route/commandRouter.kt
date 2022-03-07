package route

import interact.commands.entities.HelpCommand
import interact.commands.entities.ParsableCommand
import interact.commands.entities.StartCommand
import interact.i18n.LanguageContainer

fun matchCommand(command: String, languageContainer: LanguageContainer): Result<ParsableCommand> =
    when (command) {
        languageContainer.helpCommand() -> Result.success(HelpCommand)
        languageContainer.rankCommand() -> Result.success(StartCommand)
        else -> Result.failure(Exception("Command mismatch: $command"))
    }
