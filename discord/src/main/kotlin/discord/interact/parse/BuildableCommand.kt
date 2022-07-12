package discord.interact.parse

import core.interact.i18n.LanguageContainer
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Set<BuildableCommand> =
    setOf(
        HelpCommandParser, SettingsCommandParser,
        StyleCommandParser, LangCommandParser,
        StartCommandParser, ResignCommandParser, SetCommandParser,
        RankCommandParser, RatingCommandParser
    )

interface BuildableCommand {

    fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer): CommandListUpdateAction

}
