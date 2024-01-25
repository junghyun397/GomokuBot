package discord.interact.parse

import core.interact.i18n.LanguageContainer
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Set<BuildableCommand> =
    setOf(
        HelpCommandParser, SettingsCommandParser,
        StyleCommandParser, LangCommandParser,
        StartCommandParser, ResignCommandParser, SetCommandParser,
        RankCommandParser, ReplayListCommandParser, RatingCommandParser
    )

val engBuildableCommands: Set<BuildableCommand> =
    buildableCommands - HelpCommandParser

interface BuildableCommand {

    data class Usage(val usage: String, val description: String)

    fun getLocalizedName(container: LanguageContainer): String

    fun getLocalizedUsages(container: LanguageContainer): List<Usage>

    fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer): CommandListUpdateAction

}
