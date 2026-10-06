package discord.interact.parse

import arrow.core.Either
import core.database.DatabaseConnection
import core.interact.commands.Command
import core.interact.parse.CommandParser
import core.interact.parse.ParseFailure
import core.session.SessionPool
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface ParsableCommand : CommandParser {

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<ParseFailure, Command>

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<ParseFailure, Command>

}
