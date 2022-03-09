package interact.commands

import interact.commands.entities.Command
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface ParsableCommand {

    fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Result<Command>

    fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Result<Command>

}