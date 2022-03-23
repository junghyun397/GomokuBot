package interact.commands

import interact.commands.entities.Command
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utility.Either

interface ParsableCommand {

    val name: String

    fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure>

    fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure>

}
