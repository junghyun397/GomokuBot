package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.DebugCommand
import core.interact.commands.DebugType
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.structs.Either
import utils.structs.IO

object DebugCommandParser : NamedParser, ParsableCommand {

    override val name = "debug"

    private fun matchType(option: String): DebugType? =
        DebugType.values().firstOrNull { it.name == option }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        if (!GuildManager.hasDebugPermission(context.event.author))
            return Either.Right(this.asParseFailure("tester permission not granted", context.user) { _, _, _ ->
                IO { emptyList() }
            })

        val type = run { if (payload.size < 2) null else matchType(payload.component2().uppercase()) }
            ?: return Either.Right(this.asParseFailure("unknown debug type", context.user) { _,  _, _ ->
                IO { emptyList() }
            })

        return Either.Left(DebugCommand(
            "debug", type,
            context.event.message.contentRaw
                .drop(
                    context.event.message.contentRaw
                        .split(" ")
                        .take(2)
                        .fold(0) { acc, s -> acc + s.length }
                )
                .ifEmpty { null }
        ))
    }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("slash commands not supported", context.user) { _, _, _ ->
            IO { emptyList() }
        })

}
