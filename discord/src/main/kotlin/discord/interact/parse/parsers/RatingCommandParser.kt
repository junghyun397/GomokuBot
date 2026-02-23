package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.raise.effect
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object RatingCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "rating"

    override fun getLocalizedName(container: LanguageContainer) = container.ratingCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.ratingCommand()}`` or ``$COMMAND_PREFIX${container.ratingCommand()}``",
            description = container.commandUsageRating()
        ),
    )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command> =
        Either.Left(this.asParseFailure("not yet implemented", context.guild, context.user) { messagingService, publisher, container ->
            effect {
                messagingService.buildNotYetImplemented(publisher, container)
                    .launch()()
                emptyList()
            }
        })

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command> =
        Either.Left(this.asParseFailure("not yet implemented", context.guild, context.user) { messagingService, publisher, container ->
            effect {
                messagingService.buildNotYetImplemented(publisher, container)
                    .launch()()
                emptyList()
            }
        })

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.ratingCommand(),
            container.ratingCommandDescription()
        ) {
            option<net.dv8tion.jda.api.entities.User>(
                container.ratingCommandOptionUser(),
                container.ratingCommandOptionUserDescription(),
                false
            )
        }

}
