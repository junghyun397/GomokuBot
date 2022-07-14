package discord.interact.parse.parsers

import core.assets.User
import core.interact.commands.Command
import core.interact.commands.StyleCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.BoardStyle
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.flatMap
import utils.structs.map

object StyleCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "style"

    private fun matchStyle(option: String): BoardStyle? =
        BoardStyle.values().firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

    private fun composeMissMatchFailure(user: User): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("option mismatch", user) { producer, publisher, container ->
            producer.produceStyleNotFound(publisher, container, user)
                .map { it.launch() }
                .flatMap { producer.produceStyleGuide(publisher, container) }
                .map { it.launch(); emptyList() }
        })

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val style = context.event.getOption(context.config.language.container.styleCommandOptionCode())?.asString?.uppercase()?.let {
            matchStyle(it)
        } ?: return this.composeMissMatchFailure(context.user)

        return Either.Left(StyleCommand(context.config.language.container.styleCommand(), style))
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        val style = payload
            .getOrNull(1)
            ?.uppercase()
            ?.let { matchStyle(it) }
            ?: return this.composeMissMatchFailure(context.user)

        return Either.Left(StyleCommand(context.config.language.container.styleCommand(), style))
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
                BoardStyle.values().fold(this) { builder, style ->
                    builder.choice(
                        style.sample.styleShortcut,
                        style.sample.styleShortcut
                    )
                }
            }
        }

}
