package interact.commands

import interact.commands.entities.*
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Collection<BuildableCommand> =
    listOf(HelpCommand, StartCommand, StyleCommand, LangCommand, ResignCommand, RankCommand, SetCommand, RatingCommand)

interface BuildableCommand {

    fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer): CommandListUpdateAction

}
