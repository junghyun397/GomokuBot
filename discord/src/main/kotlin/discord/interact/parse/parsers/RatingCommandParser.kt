package discord.interact.parse.parsers

import core.interact.Order
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object RatingCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "rating"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.event.user.extractUser()) { producer, publisher, container ->
            producer.produceNotYetImplemented(publisher, container).map { it.launch(); Order.Unit }
        })

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.event.author.extractUser()) { producer, publisher, container ->
            producer.produceNotYetImplemented(publisher, container).map { it.launch(); Order.Unit }
        })

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.ratingCommand(),
            languageContainer.ratingCommandDescription()
        ) {
            option<net.dv8tion.jda.api.entities.User>(
                languageContainer.ratingCommandOptionUser(),
                languageContainer.ratingCommandOptionUserDescription(),
                false
            )
        }

}
