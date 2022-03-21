package interact.commands.entities

import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import interact.reports.asCommandReport
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
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
    ) = runCatching {
        this.asCommandReport("$userId request to $opponent")
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

        override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
            action.slash(
                languageContainer.startCommand(),
                languageContainer.startCommandDescription()
            ) {
                option<User>(
                    languageContainer.startCommandOptionOpponent(),
                    languageContainer.startCommandOptionOpponentDescription(),
                    false
                )
            }

    }

}
