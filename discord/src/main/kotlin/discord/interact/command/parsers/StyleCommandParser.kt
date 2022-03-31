package discord.interact.command.parsers

import core.assets.Order
import core.interact.commands.Command
import core.interact.commands.StyleCommand
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardStyle
import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.interact.InteractionContext
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import discord.interact.command.ParseFailure
import discord.interact.command.asParseFailure
import discord.interact.message.DiscordMessageBinder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object StyleCommandParser : ParsableCommand, BuildableCommand {

    override val name = "style"

    private fun matchStyle(option: String): BoardStyle? =
        BoardStyle.values().firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, ParseFailure> {
        val style = context.event.getOption(context.config.language.container.styleCommandOptionCode())?.asString?.uppercase()?.let {
            matchStyle(it)
        }
            ?: return Either.Right(this.asParseFailure("option missmatch") { container, publisher ->
                DiscordMessageBinder.bindStyleGuide(publisher, container).map { it.launch(); Order.UNIT }
            })

        return Either.Left(StyleCommand(context.config.language.container.styleCommand(), style))
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure> {
        val option = context.event.message.contentRaw
            .drop(context.config.language.container.styleCommand().length + 2)
            .uppercase()

        val style = matchStyle(option)
            ?: return Either.Right(this.asParseFailure("option missmatch") { container, publisher ->
                DiscordMessageBinder.bindStyleGuide(publisher, container).map { it.launch(); Order.UNIT }
            })

        return Either.Left(StyleCommand(context.config.language.container.styleCommand(), style))
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
                        style.sample.styleShortcut,
                        style.sample.styleShortcut
                    )
                }
            }
        }

}
