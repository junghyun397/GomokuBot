package discord.interact.command.parsers

import core.assets.Order
import core.interact.commands.Command
import core.interact.commands.LangCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
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

object LangCommandParser : ParsableCommand, BuildableCommand {

    override val name = "lang"

    private fun matchLang(option: String): Language? =
        Language.values().firstOrNull { it.container.languageCode() == option }

    private val missMatchFailure =
        Either.Right(this.asParseFailure("option missmatch") { _, publisher ->
            DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT }
        })

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, ParseFailure> {
        val lang = context.event.getOption(context.config.language.container.languageCommandOptionCode())?.asString?.uppercase()?.let {
            matchLang(it)
        } ?: return missMatchFailure

        return Either.Left(LangCommand(context.config.language.container.langCommand(), lang))
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure> {
        val option = context.event.message.contentRaw
            .drop(context.config.language.container.langCommand().length + 2)
            .uppercase()
        val lang = matchLang(option) ?: return missMatchFailure

        return Either.Left(LangCommand(context.config.language.container.langCommand(), lang))
    }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.langCommand(),
            languageContainer.langCommandDescription(),
        ) {
            option<String>(
                languageContainer.languageCommandOptionCode(),
                languageContainer.languageCommandOptionCodeDescription(),
                true
            ) {
                Language.values().fold(this) { builder, language ->
                    builder.choice(
                        language.container.languageCode(),
                        language.container.languageCode()
                    )
                }
            }
        }

}
