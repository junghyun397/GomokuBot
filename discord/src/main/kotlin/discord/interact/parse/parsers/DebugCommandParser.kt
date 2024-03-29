package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.DebugCommand
import core.interact.commands.DebugType
import core.interact.emptyOrders
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
import discord.interact.GuildManager
import discord.interact.UserInteractionContext
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.structs.Either
import utils.structs.IO

object DebugCommandParser : CommandParser, ParsableCommand {

    override val name = "debug"

    private fun matchType(option: String): DebugType? =
        DebugType.entries.firstOrNull { it.name == option }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        if (!GuildManager.hasDebugPermission(context.discordConfig, context.event.author))
            return Either.Right(this.asParseFailure("tester permission not granted", context.guild, context.user) { _, _, _ ->
                IO.value(emptyOrders)
            })

        val type = run { when {
            payload.size < 2 -> null
            else -> matchType(payload.component2().uppercase())
        } }
            ?: return Either.Right(this.asParseFailure("unknown debug type", context.guild, context.user) { _,  _, _ ->
                IO.value(emptyOrders)
            })

        val customPayload = when (type) {
            DebugType.STATUS ->
                "node = #${context.event.jda.shardInfo.shardId}, ${context.event.jda.guildCache.size()} guilds, ${context.event.jda.userCache.size()} users"
            else -> null
        }

        return Either.Left(DebugCommand(type, customPayload ?: payload.joinToString(separator = " ")))
    }

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("slash commands not supported", context.guild, context.user) { _, _, _ ->
            IO.value(emptyOrders)
        })

}
