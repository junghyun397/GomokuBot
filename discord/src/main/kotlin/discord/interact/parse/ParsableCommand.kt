package discord.interact.parse

import arrow.core.Either
import core.interact.commands.Command
import core.interact.parse.CommandParser
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface ParsableCommand : CommandParser {

    suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command>

    suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command>

}
