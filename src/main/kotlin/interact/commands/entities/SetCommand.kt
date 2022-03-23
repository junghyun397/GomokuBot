package interact.commands.entities

import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.commands.ParseFailure
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class SetCommand(override val command: String) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ) = runCatching {
        TODO()
    }

    companion object : ParsableCommand, BuildableCommand {

        override val name = "s"

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
            TODO("Not yet implemented")
        }

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
            TODO("Not yet implemented")
        }

        override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
            action.slash(
                "s",
                "set"
            ) {
                option<String>("row", "alphabet", true)
                option<Int>("column", "number", true)
            }

    }

}