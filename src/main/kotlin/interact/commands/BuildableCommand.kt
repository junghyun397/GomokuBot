package interact.commands

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface BuildableCommand {

    fun buildCommandData(languageContainer: LanguageContainer): CommandData

}