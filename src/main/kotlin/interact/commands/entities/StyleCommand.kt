package interact.commands.entities

import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import route.BotContext
import session.entities.GuildConfig
import utility.MessagePublisher
import utility.UserId

class StyleCommand(override val name: String) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ) = TODO()

    companion object : ParsableCommand, BuildableCommand {

        override fun parse(
            event: SlashCommandInteractionEvent,
            languageContainer: LanguageContainer
        ) = TODO()

        override fun parse(
            event: MessageReceivedEvent,
            languageContainer: LanguageContainer
        ) = TODO()

        override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
            action.slash(
                languageContainer.styleCommand(),
                languageContainer.styleCommandDescription()
            ) {
                option<String>(
                    languageContainer.styleCommandOptionCode(),
                    languageContainer.styleCommandOptionCodeDescription(),
                    true
                ) {
                    choice("IMAGE", "IMAGE")
                    choice("TEXT", "TEXT")
                    choice("SOLID TEXT", "SOLID TEXT")
                    choice("UNICODE TEXT", "UNICODE TEXT")
                }
            }

    }

}