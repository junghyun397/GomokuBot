package discord.interact.parse.parsers

import core.interact.commands.HelpCommand
import core.interact.i18n.Language
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

object HelpCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "help"

    private fun checkCrossLanguageCommand(container: LanguageContainer, command: String) =
        container.helpCommand() != command

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(
            HelpCommand(
                context.config.language.container.helpCommand(),
                this.checkCrossLanguageCommand(context.config.language.container, context.event.name.lowercase())
            )
        )

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(
            HelpCommand(
                context.config.language.container.helpCommand(),
                this.checkCrossLanguageCommand(context.config.language.container, payload[0].lowercase())
            )
        )

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.helpCommand(),
                container.helpCommandDescription()
            )

            if (container.helpCommand() != "help") {
                slash(
                    "help",
                    Language.ENG.container.helpCommandDescription()
                )
            }
        }

}
