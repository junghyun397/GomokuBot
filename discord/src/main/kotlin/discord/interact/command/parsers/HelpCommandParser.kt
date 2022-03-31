package discord.interact.command.parsers

import core.interact.commands.HelpCommand
import core.interact.i18n.LanguageContainer
import dev.minn.jda.ktx.interactions.slash
import discord.interact.InteractionContext
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object HelpCommandParser : ParsableCommand, BuildableCommand {

    override val name = "help"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(HelpCommand(context.config.language.container.helpCommand()))

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        Either.Left(HelpCommand(context.config.language.container.helpCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.helpCommand(),
            languageContainer.helpCommandDescription()
        )

}
