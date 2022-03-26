package discord.interact.command.parsers

import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.ParseFailure
import core.interact.commands.Command
import core.interact.commands.StyleCommand
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardStyle
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import discord.interact.command.asParseFailure
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either
import utils.monads.IO

object StyleCommandParser : ParsableCommand, BuildableCommand {

    override val name = "style"

    private fun matchStyle(option: String): BoardStyle? =
        BoardStyle.values().firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

    override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        val style = event.getOption(languageContainer.styleCommandOptionCode())?.asString?.uppercase()?.let {
            matchStyle(it)
        }
            ?: return Either.Right(this.asParseFailure("option missmatch") { _, _ ->
                IO {}
            })

        return Either.Left(StyleCommand(languageContainer.styleCommand(), style))
    }

    override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        val option = event.message.contentRaw
            .drop(languageContainer.styleCommand().length + 2)
            .uppercase()

        val style = matchStyle(option)
            ?: return Either.Right(this.asParseFailure("option missmatch") { container, publisher ->
                IO {}
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
