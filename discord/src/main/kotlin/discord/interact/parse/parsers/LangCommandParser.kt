package discord.interact.parse.parsers

import core.assets.User
import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.LangCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.choice
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import discord.interact.message.DiscordMessageProducer
import discord.interact.parse.DiscordParseFailure
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either

object LangCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "lang"

    private fun matchLang(option: String): Language? =
        Language.values().firstOrNull { it.container.languageCode() == option }

    private fun composeMissMatchFailure(user: User): Either<Command, DiscordParseFailure> =
        Either.Right(this.asParseFailure("option missmatch", user) { producer, publisher, _ ->
            producer.produceLanguageNotFound(publisher).map { it.launch() }
                .flatMap {
                    producer.produceLanguageGuide(publisher).map { it.launch(); Order.Unit }
                }
        })

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val lang = context.event.getOption(context.config.language.container.languageCommandOptionCode())?.asString?.uppercase()?.let {
            matchLang(it)
        } ?: return this.composeMissMatchFailure(context.event.user.extractUser())

        return Either.Left(LangCommand(context.config.language.container.languageCommand(), lang))
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, DiscordParseFailure> {
        val option = context.event.message.contentRaw
            .drop(context.config.language.container.languageCommand().length + 2)
            .uppercase()
        val lang = matchLang(option) ?: return this.composeMissMatchFailure(context.event.author.extractUser())

        return Either.Left(LangCommand(context.config.language.container.languageCommand(), lang))
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.languageCommand(),
            container.languageCommandDescription(),
        ) {
            option<String>(
                container.languageCommandOptionCode(),
                container.languageCommandOptionCodeDescription(),
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
