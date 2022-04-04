package discord.interact.parse.parsers

import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.DebugCommand
import core.interact.commands.DebugType
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
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

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, DiscordParseFailure> {
        val option = context.event.message.contentRaw
            .drop(this.name.length + 2)
            .uppercase()

        val type = matchType(option)
            ?: return Either.Right(this.asParseFailure("unknown debug type") {_,  _, _ -> IO { Order.Unit } })

        return Either.Left(DebugCommand(
            "debug", type,
            context.event.message.contentRaw.split(" ").drop(1).firstOrNull()
        ))
    }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("slash not supported") { _, _, _ -> IO { Order.Unit } })

}
