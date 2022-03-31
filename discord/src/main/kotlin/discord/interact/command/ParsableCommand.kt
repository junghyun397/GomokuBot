package discord.interact.command

import core.assets.Order
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessagePublisher
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.monads.Either
import utils.monads.IO

interface ParsableCommand {

    val name: String

    suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, ParseFailure>

    suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure>

}

fun ParsableCommand.asParseFailure(comment: String, onFailure: (LanguageContainer, DiscordMessagePublisher) -> IO<Order>) =
    ParseFailure(this.name, comment, onFailure)
