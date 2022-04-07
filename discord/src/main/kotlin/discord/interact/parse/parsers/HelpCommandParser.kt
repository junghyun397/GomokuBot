package discord.interact.parse.parsers

import core.interact.commands.HelpCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.i18n.LanguageENG
import core.interact.parse.NamedParser
import dev.minn.jda.ktx.interactions.slash
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object HelpCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "help"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(HelpCommand(context.config.language.container.helpCommand()))

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        Either.Left(HelpCommand(context.config.language.container.helpCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.apply {
            if (languageContainer !is LanguageENG) slash(
                "help",
                Language.ENG.container.helpCommandDescription()
            )
            slash(
                languageContainer.helpCommand(),
                languageContainer.helpCommandDescription()
            )
        }

}
