package interact.commands.entities

import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import interact.commands.BuildableCommand
import interact.commands.ParsableCommand
import interact.commands.ParseFailure
import interact.commands.asParseFailure
import interact.i18n.Language
import interact.i18n.LanguageContainer
import interact.reports.asCommandReport
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import route.BotContext
import session.entities.GuildConfig
import utility.Either
import utility.MessagePublisher
import utility.UserId

class LangCommand(override val command: String, private val language: Language) : Command {

    override suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ) = runCatching {
        this.asCommandReport("${guildConfig.language.name} to ${language.name}")
    }

    companion object : ParsableCommand, BuildableCommand {

        override val name = "lang"

        private fun matchLang(option: String): Language? =
            Language.values().firstOrNull { it.container.languageCode() == option }

        override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
            val lang = event.getOption(languageContainer.languageCommandOptionCode())?.asString?.uppercase()?.let {
                matchLang(it)
            }
                ?: return Either.Right(this.asParseFailure("option missmatch") { container, publisher ->
                    // TODO
                })

            return Either.Left(LangCommand(languageContainer.langCommand(), lang))
        }

        override fun parse(
            event: MessageReceivedEvent,
            languageContainer: LanguageContainer
        ): Either<Command, ParseFailure> {
            val option = event.message.contentRaw
                .drop(languageContainer.langCommand().length + 2)
                .uppercase()

            val lang = matchLang(option)
                ?: return Either.Right(this.asParseFailure("option missmatch") { container, pulisher ->
                    // TODO
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

}