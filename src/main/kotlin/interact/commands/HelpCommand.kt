package interact.commands

import BotContext
import interact.i18n.LanguageContainer
import interact.message.MessageAgent
import interact.reports.CommandReport
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import utility.MessagePublisher

class HelpCommand(override val name: String = "help") : Command {

    override fun buildCommandData(languageContainer: LanguageContainer): CommandData =
        slash(languageContainer.helpCommand(), languageContainer.helpCommandDescription())

    override suspend fun execute(botContext: BotContext, user: User, messagePublisher: MessagePublisher): Result<CommandReport> =
        runCatching {
            MessageAgent.sendHelpAbout(messagePublisher)
            MessageAgent.sendHelpCommand(messagePublisher)
            MessageAgent.sendHelpSkin(messagePublisher)
            MessageAgent.sendHelpLanguage(messagePublisher)

            TODO()
        }

    companion object : ParsableCommand {

        override fun parse(event: SlashCommandInteractionEvent): Result<Command> =
            Result.success(HelpCommand())

        override fun parse(event: MessageReceivedEvent): Result<Command> =
            Result.success(HelpCommand())

    }

}