package discord.interact.parse

import core.interact.commands.Command
import core.interact.parse.NamedParser
import discord.interact.InteractionContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.structs.Either

interface ParsableCommand : NamedParser {

    suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure>

    suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure>

}
