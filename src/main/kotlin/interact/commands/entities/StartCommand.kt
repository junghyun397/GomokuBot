package interact.commands.entities

import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import route.BotContext
import utility.GuildId
import utility.MessagePublisher
import utility.UserId

class StartCommand(override val name: String = "start", val opponent: UserId?) : Command {

    override suspend fun execute(
        botContext: BotContext,
        languageContainer: LanguageContainer,
        user: UserId,
        messagePublisher: MessagePublisher
    ): Result<CommandReport> = TODO("Not yet implemented")

    companion object : ParsableCommand, BuildableCommand {

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) = Result.runCatching {
            StartCommand(
                opponent = event.getOption(languageContainer.startCommand())!!.asUser.let {
                    if (it.isBot) null
                    else UserId(it.idLong)
                }
            )
        }

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) = Result.runCatching {
            StartCommand(
                opponent = event.message.mentionedUsers.let {
                    if (it.isEmpty() || it[0].isBot) null
                    else UserId(it[0].idLong)
                }
            )
        }

        override fun buildCommandData(languageContainer: LanguageContainer): CommandData =
            slash(languageContainer.startCommand(), languageContainer.startCommandDescription())
                .addOption(
                    OptionType.USER,
                    languageContainer.startCommandOptionOpponent(),
                    languageContainer.startCommandOptionOpponentDescription(),
                    false
                )

    }

}
