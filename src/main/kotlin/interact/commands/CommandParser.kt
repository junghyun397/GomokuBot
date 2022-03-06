package interact.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface CommandParser {

    fun parse(event: SlashCommandInteractionEvent): Result<Command>

    fun parse(event: MessageReceivedEvent): Result<Command>

}