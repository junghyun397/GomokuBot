package interact.commands.entities

import dev.minn.jda.ktx.interactions.slash
import interact.GuildManager
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.i18n.LanguageContainer
import interact.message.MessageAgent
import interact.message.graphics.TextBoardRenderer
import interact.reports.asCommandReport
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class HelpCommand(override val command: String, private val guild: Guild) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher,
    ) = runCatching {
        val helpSent = MessageAgent.sendEmbedAbout(messagePublisher, guildConfig.language.container)
            .flatMap { MessageAgent.sendEmbedCommand(messagePublisher, guildConfig.language.container) }
            .flatMap { MessageAgent.sendEmbedStyle(messagePublisher, guildConfig.language.container) }
            .flatMap { MessageAgent.sendEmbedLanguage(messagePublisher) }
            .fold(
                onDefined = { true },
                onEmpty = { false }
            )

        // --- TODO: REMOVE

        GuildManager.insertCommands(guild, guildConfig.language.container)

        TextBoardRenderer().attachBoardWithButtons(messagePublisher)

        // ---

        return@runCatching this.asCommandReport("helpSent = $helpSent")
    }

    companion object : ParsableCommand, BuildableCommand {

        override val name = "help"

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
            Either.Left(HelpCommand(languageContainer.helpCommand(), event.guild!! /* TODO: REMOVE */ ))

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
            Either.Left(HelpCommand(languageContainer.helpCommand(), event.guild /* TODO: REMOVE */ ))

        override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
            action.slash(
                languageContainer.helpCommand(),
                languageContainer.helpCommandDescription()
            )

    }

}
