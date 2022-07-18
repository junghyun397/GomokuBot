package discord.interact.parse.parsers

import core.interact.commands.SettingsCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object SettingsCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "config"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(SettingsCommand(context.config.language.container.settingsCommand()))

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(SettingsCommand(context.config.language.container.settingsCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.settingsCommand(),
                container.settingsCommandDescription()
            )
        }

}