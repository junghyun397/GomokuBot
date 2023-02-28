package discord.interact.parse.parsers

import core.interact.commands.HelpCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object HelpCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "help"

    override fun getLocalizedName(container: LanguageContainer) = container.helpCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.helpCommand()}`` or ``$COMMAND_PREFIX${container.helpCommand()}``",
            description = container.commandUsageHelp()
        )
    )

    private fun checkCrossLanguageCommand(container: LanguageContainer, command: String) =
        container.helpCommand() != command

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(
            HelpCommand(this.checkCrossLanguageCommand(context.config.language.container, context.event.name.lowercase()))
        )

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(
            HelpCommand(this.checkCrossLanguageCommand(context.config.language.container, payload[0].lowercase()))
        )

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            if (container != Language.ENG.container) slash(
                container.helpCommand(),
                container.helpCommandDescription()
            )
        }

}
