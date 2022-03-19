package interact.commands.entities

import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import interact.message.MessageAgent
import interact.reports.CommandReport
import interact.reports.toCommandReport
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class HelpCommand(override val name: String = "help") : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher,
    ): Result<CommandReport> =
        runCatching {
            MessageAgent.sendHelpAbout(messagePublisher, guildConfig.language.container)
            MessageAgent.sendHelpCommand(messagePublisher, guildConfig.language.container)
            MessageAgent.sendHelpStyle(messagePublisher, guildConfig.language.container)
            MessageAgent.sendHelpLanguage(messagePublisher, guildConfig.language.container)

            this.toCommandReport("succeed")
        }

    companion object : ParsableCommand, BuildableCommand {

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
            Either.Left(HelpCommand())

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
            Either.Left(HelpCommand())

        override fun buildCommandData(languageContainer: LanguageContainer): CommandData =
            slash(languageContainer.helpCommand(), languageContainer.helpCommandDescription())

    }

}
