package discord.interact.command.parsers

import core.interact.commands.Command
import core.interact.commands.LangCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
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

    override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        val lang = event.getOption(languageContainer.languageCommandOptionCode())?.asString?.uppercase()?.let {
            matchLang(it)
        }
            ?: return Either.Right(this.asParseFailure("option missmatch") { _, publisher ->
                DiscordMessageBinder.bindLanguageGuide(publisher).map { it.retrieve() }
            })

        return Either.Left(LangCommand(languageContainer.langCommand(), lang))
    }

    override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        val option = event.message.contentRaw
            .drop(languageContainer.langCommand().length + 2)
            .uppercase()

        val lang = matchLang(option)
            ?: return Either.Right(this.asParseFailure("option missmatch") { _, publisher ->
                DiscordMessageBinder.bindLanguageGuide(publisher).map { it.retrieve() }
            })

        return Either.Left(LangCommand(languageContainer.langCommand(), lang))
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
                        "${language.container.languageCode()}:${language.container.languageName()}",
                        language.container.languageCode()
                    )
                }
            }
        }

}
