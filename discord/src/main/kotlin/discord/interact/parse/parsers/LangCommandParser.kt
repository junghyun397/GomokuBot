package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.raise.effect
import core.assets.Guild
import core.assets.User
import core.interact.commands.Command
import core.interact.commands.LangCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
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

object LangCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "lang"

    private val languageList =
        buildString {
            Language.entries.forEach { language ->
                append(" ``${language.container.languageCode()}``")
            }
        }

    override fun getLocalizedName(container: LanguageContainer) = container.languageCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.languageCommand()}`` or ``$COMMAND_PREFIX${container.languageCommand()}``",
            description = container.commandUsageLang(this.languageList)
        ),
    )

    private fun matchLang(option: String): Language? =
        Language.entries.firstOrNull { it.container.languageCode() == option }

    private fun composeMissMatchFailure(guild: Guild, user: User): Either<DiscordParseFailure, Command> =
        Either.Left(this.asParseFailure("option mismatch", guild, user) { messagingService, publisher, _ ->
            effect {
                messagingService.buildLanguageNotFound(publisher).launch()()
                messagingService.buildLanguageGuide(publisher).launch()()
                emptyList()
            }
        })

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command> {
        val lang = context.event.getOption(context.config.language.container.languageCommandOptionCode())?.asString?.uppercase()?.let {
            matchLang(it)
        } ?: return this.composeMissMatchFailure(context.guild, context.user)

        return Either.Right(LangCommand(lang))
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command> {
        val lang = payload
            .getOrNull(1)
            ?.uppercase()
            ?.let { matchLang(it) }
            ?: return this.composeMissMatchFailure(context.guild, context.user)

        return Either.Right(LangCommand(lang))
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
                Language.entries.fold(this) { builder, language ->
                    builder.choice(
                        language.container.languageCode(),
                        language.container.languageCode()
                    )
                }
            }
        }

}
