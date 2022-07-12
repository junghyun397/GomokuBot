package discord.interact.parse.parsers

import core.interact.Order
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.map

object RatingCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "rating"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.user) { producer, publisher, container ->
            producer.produceNotYetImplemented(publisher, container, "https://discord.gg/vq8pkfF")
                .map { it.launch(); Order.Unit }
        })

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("not yet implemented", context.user) { producer, publisher, container ->
            producer.produceNotYetImplemented(publisher, container, "https://discord.gg/vq8pkfF")
                .map { it.launch(); Order.Unit }
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
