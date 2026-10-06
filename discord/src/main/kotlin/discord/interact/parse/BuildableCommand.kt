package discord.interact.parse

import core.interact.i18n.LanguageContainer
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Set<BuildableCommand> =
    setOf(
        HelpCommandParser, SettingsCommandParser,
        LangCommandParser,
        StartCommandParser, ResignCommandParser, SetCommandParser, UndoCommandParser, BoardCommandParser,
        RankCommandParser, ReplayListCommandParser, RatingCommandParser
    )

interface BuildableCommand {

    data class Usage(val usage: String, val description: String)

    fun getLocalizedName(container: LanguageContainer): String

    fun getLocalizedUsages(container: LanguageContainer): List<Usage>

    fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer): CommandListUpdateAction

}
