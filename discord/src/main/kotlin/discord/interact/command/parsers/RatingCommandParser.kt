package discord.interact.command.parsers

import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.ParseFailure
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import discord.interact.InteractionContext
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object RatingCommandParser : ParsableCommand, BuildableCommand {

    override val name = "rating"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, ParseFailure> {
        TODO("Not yet implemented")
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure> {
        TODO("Not yet implemented")
    }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.ratingCommand(),
            languageContainer.ratingCommandDescription()
        ) {

        }

}
