package discord.interact.parse.parsers

import core.assets.User
import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.StyleCommand
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardStyle
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object StyleCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "style"

    private fun matchStyle(option: String): BoardStyle? =
        BoardStyle.values().firstOrNull { it.sample.styleShortcut == option || it.sample.styleName == option }

    private fun composeMissMatchFailure(user: User): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("option missmatch", user) { producer, publisher, container ->
            producer.produceStyleNotFound(publisher, container).map { it.launch() }
                .flatMap { producer.produceStyleGuide(publisher, container) }.map { it.launch(); Order.Unit }
        })

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val style = context.event.getOption(context.config.language.container.styleCommandOptionCode())?.asString?.uppercase()?.let {
            matchStyle(it)
        } ?: return this.composeMissMatchFailure(context.event.user.extractUser())

        return Either.Left(StyleCommand(context.config.language.container.styleCommand(), style))
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, DiscordParseFailure> {
        val option = context.event.message.contentRaw
            .drop(context.config.language.container.styleCommand().length + 2)
            .uppercase()

        val style = matchStyle(option) ?: return this.composeMissMatchFailure(context.event.author.extractUser())

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
