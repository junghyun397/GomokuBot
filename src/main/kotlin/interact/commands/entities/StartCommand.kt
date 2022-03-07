package interact.commands.entities

import interact.commands.CommandReport
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import utility.MessagePublisher

class StartCommand(override val name: String = "start", val opponent: User) : Command {

    override fun buildCommandData(languageContainer: LanguageContainer): CommandData = TODO("Not yet implemented")

    override suspend fun execute(user: User, messagePublisher: MessagePublisher): Result<CommandReport> = TODO("Not yet implemented")

    companion object : ParsableCommand {

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