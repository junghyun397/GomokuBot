package discord.interact.parse.parsers

import core.interact.commands.SettingsCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object SettingsCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "settings"

    override fun getLocalizedName(container: LanguageContainer) = container.settingsCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.settingsCommand()}`` or ``$COMMAND_PREFIX${container.settingsCommand()}``",
            description = container.commandUsageSettings()
        ),
    )

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(SettingsCommand())

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(SettingsCommand())

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.settingsCommand(),
                container.settingsCommandDescription()
            )
        }

}
