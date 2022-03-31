package discord.interact.command.parsers

import core.assets.Order
import core.interact.commands.Command
import core.interact.commands.DebugCommand
import core.interact.commands.DebugType
import core.interact.i18n.LanguageContainer
import discord.interact.InteractionContext
import discord.interact.command.ParsableCommand
import discord.interact.command.ParseFailure
import discord.interact.command.asParseFailure
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.monads.Either
import utils.monads.IO

object DebugCommandParser : ParsableCommand {

    override val name = "debug"

    private fun matchType(option: String): DebugType? =
        DebugType.values().firstOrNull { it.name == option }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure> {
        val option = context.event.message.contentRaw
            .drop(this.name.length + 2)
            .uppercase()

        val type = matchType(option)
            ?: return Either.Right(this.asParseFailure("unknown debug type") { _, _ -> IO { Order.UNIT } })

        return Either.Left(DebugCommand(
            "debug", type,
            context.event.message.contentRaw.split(" ").drop(1).firstOrNull()
        ))
    }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Right(this.asParseFailure("slash not supported") { _, _ -> IO { Order.UNIT } })

}
