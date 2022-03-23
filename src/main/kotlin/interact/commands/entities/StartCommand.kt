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
import utility.extractId

class StartCommand(override val command: String = "start", val opponent: UserId?) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ) = runCatching {
        this.asCommandReport("$userId request to $opponent")
    }

    companion object : ParsableCommand, BuildableCommand {

        override val name = "start"

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
            Either.Left(
                StartCommand(
                    opponent = event.getOption(languageContainer.startCommandOptionOpponent())?.let {
                        val user = it.asUser
                        if (user.isBot) null
                        else user.extractId()
                    }
                )
            )

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
            Either.Left(
                StartCommand(
                    opponent = event.message.mentionedUsers
                        .firstOrNull { !it.isBot && it.idLong != event.author.idLong }
                        ?.extractId()
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
