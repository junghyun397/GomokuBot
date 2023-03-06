package discord.interact.parse.parsers

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
import utils.structs.Either
import utils.structs.map

object RatingCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "rating"

    override fun getLocalizedName(container: LanguageContainer) = container.ratingCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.ratingCommand()}`` or ``$COMMAND_PREFIX${container.ratingCommand()}``",
            description = container.commandUsageRating()
        ),
    )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.guild, context.user) { messagingService, publisher, container ->
            messagingService.buildNotYetImplemented(publisher, container, "https://discord.gg/vq8pkfF")
                .launch()
                .map { emptyList() }
        })

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.guild, context.user) { messagingService, publisher, container ->
            messagingService.buildNotYetImplemented(publisher, container, "https://discord.gg/vq8pkfF")
                .launch()
                .map { emptyList() }
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
