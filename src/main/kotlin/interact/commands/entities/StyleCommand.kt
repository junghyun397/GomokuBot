package interact.commands.entities

import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.commands.ParseFailure
import interact.commands.asParseFailure
import interact.i18n.LanguageContainer
import interact.message.graphics.BoardStyle
import interact.reports.asCommandReport
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class StyleCommand(override val command: String, private val style: BoardStyle) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ) = runCatching {
        this.asCommandReport("${guildConfig.boardStyle.name} to ${style.name}")
    }

    companion object : ParsableCommand, BuildableCommand {

        override val name = "style"

        private fun matchStyle(option: String): BoardStyle? =
            BoardStyle.values().firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
            val style = event.getOption(languageContainer.styleCommandOptionCode())?.asString?.uppercase()?.let {
                matchStyle(it)
            }
                ?: return Either.Right(this.asParseFailure("option missmatch") { _, _ ->
                    // TODO(internal error)
                })

            return Either.Left(StyleCommand(languageContainer.styleCommand(), style))
        }

        override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
            val option = event.message.contentRaw
                .drop(languageContainer.styleCommand().length + 2)
                .uppercase()

            val style = matchStyle(option)
                ?: return Either.Right(this.asParseFailure("option missmatch") { container, publisher ->
                    // TODO
                })

            return Either.Left(StyleCommand(languageContainer.styleCommand(), style))
        }

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
                    BoardStyle.values().fold(this) { builder, style ->
                        builder.choice(
                            "${style.sample.styleShortcut}:${style.sample.styleName}",
                            style.sample.styleShortcut
                        )
                    }
                }
            }

    }

}
