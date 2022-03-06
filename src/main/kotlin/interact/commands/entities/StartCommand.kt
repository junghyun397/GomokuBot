package interact.commands.entities

import interact.commands.Command
import interact.commands.CommandParser
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

class StartCommand(override val name: String = "start", val opponent: User) : Command {

    override fun buildCommandData(languageContainer: LanguageContainer): CommandData = TODO("Not yet implemented")

    override fun process(user: User, channel: MessageChannel): Result<Unit> = TODO("Not yet implemented")

    companion object : CommandParser {

        override fun parse(event: SlashCommandInteractionEvent) = Result.runCatching {
            StartCommand(
                opponent = event.getOption("")!!.asUser
            )
        }

        override fun parse(event: MessageReceivedEvent) = Result.runCatching {
            StartCommand(
                opponent = event.message.mentionedUsers[0]
            )
        }

    }

}