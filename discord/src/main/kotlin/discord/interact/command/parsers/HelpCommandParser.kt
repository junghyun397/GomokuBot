package discord.interact.command.parsers

import core.interact.commands.HelpCommand
import core.interact.i18n.LanguageContainer
import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import discord.interact.message.DiscordButtons
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object HelpCommandParser : ParsableCommand, BuildableCommand {

    override val name = "help"

    override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
        Either.Left(HelpCommand(languageContainer.helpCommand()))

    override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
        Either.Left(HelpCommand(languageContainer.helpCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.helpCommand(),
            languageContainer.helpCommandDescription()
        )

}
