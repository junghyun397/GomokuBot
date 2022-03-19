package interact.commands.entities

import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import interact.reports.toCommandReport
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class StartCommand(override val name: String = "start", val opponent: UserId?) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ): Result<CommandReport> = runCatching {
        this.toCommandReport("$userId request to $opponent")
    }

    companion object : ParsableCommand, BuildableCommand {

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
            Either.Left(
                StartCommand(
                    opponent = event.getOption(languageContainer.startCommand())?.let {
                        val user = it.asUser
                        if (user.isBot) null
                        else UserId(user.idLong)
                    }
                )
            )

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
            Either.Left(
                StartCommand(
                    opponent = event.message.mentionedUsers.let {
                        if (it.isEmpty() || it[0].isBot || it[0].idLong == event.author.idLong) null
                        else UserId(it[0].idLong)
                    }
                )
            )

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
