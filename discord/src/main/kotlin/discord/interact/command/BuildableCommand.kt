package discord.interact.command

import core.interact.i18n.LanguageContainer
import discord.interact.command.parsers.*
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

val buildableCommands: Collection<BuildableCommand> =
    listOf(
        HelpCommandParser, StartCommandParser,
        StyleCommandParser, LangCommandParser,
        ResignCommandParser, RankCommandParser,
        SetCommandParser, RatingCommandParser
    )

interface BuildableCommand {

    fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer): CommandListUpdateAction

}
