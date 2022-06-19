package discord.interact.parse.parsers

import core.interact.commands.ConfigCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import dev.minn.jda.ktx.interactions.slash
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object ConfigCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "config"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(ConfigCommand(context.config.language.container.configCommand()))

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(ConfigCommand(context.config.language.container.configCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.configCommand(),
                container.configCommandDescription()
            )
        }

}
