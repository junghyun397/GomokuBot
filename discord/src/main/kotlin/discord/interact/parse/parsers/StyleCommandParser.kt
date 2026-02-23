package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.raise.effect
import core.interact.commands.Command
import core.interact.commands.StyleCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
import core.session.BoardStyle
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object StyleCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "style"

    override fun getLocalizedName(container: LanguageContainer) = container.styleCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.styleCommand()}`` or ``$COMMAND_PREFIX${container.styleCommand()}``",
            description = container.commandUsageStyle()
        ),
    )

    private fun matchStyle(option: String): BoardStyle? =
        BoardStyle.entries.firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

    private fun composeMissMatchFailure(context: UserInteractionContext<*>): Either<DiscordParseFailure, Command> =
        Either.Left(this.asParseFailure("option mismatch", context.guild, context.user) { messagingService, publisher, container ->
            effect {
                messagingService.buildStyleNotFound(publisher, container).launch()()
                messagingService.buildStyleGuide(publisher, container).launch()()
                emptyList()
            }
        })

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command> {
        val style = context.event.getOption(context.config.language.container.styleCommandOptionCode())?.asString?.uppercase()?.let {
            matchStyle(it)
        } ?: return this.composeMissMatchFailure(context)

        return Either.Right(StyleCommand(style))
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command> {
        val style = payload
            .getOrNull(1)
            ?.uppercase()
            ?.let { matchStyle(it) }
            ?: return this.composeMissMatchFailure(context)

        return Either.Right(StyleCommand(style))
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.styleCommand(),
            container.styleCommandDescription()
        ) {
            option<String>(
                container.styleCommandOptionCode(),
                container.styleCommandOptionCodeDescription(),
                true
            ) {
                BoardStyle.entries.fold(this) { builder, style ->
                    builder.choice(
                        style.sample.styleShortcut,
                        style.sample.styleShortcut
                    )
                }
            }
        }

}
