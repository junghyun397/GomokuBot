package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.interact.commands.Command
import core.interact.commands.LangCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.AppMessage
import core.interact.parse.CommandParser
import core.interact.parse.ParseFailure
import core.interact.parse.asParseFailure
import core.session.SessionPool
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object LangCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "lang"

    override fun getLocalizedName(container: LanguageContainer) = container.languageCommand

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "`/${container.languageCommand}` or `$COMMAND_PREFIX${container.languageCommand}`",
            description = container.commandUsageLang
        ),
    )

    private fun matchLang(option: String): Language? =
        Language.entries.firstOrNull { it.container.languageCode == option }

    private fun composeMissMatchFailure(channel: Channel, user: User.Human): Either<ParseFailure, Command> =
        Either.Left(this.asParseFailure("option mismatch", channel, user) { messagingService, publisher, _ ->
            effect {
                publisher(AppMessage.Text("There is an error in the Language Code. Please select from the list below.")).launch()()
                publisher(AppMessage.LanguageGuide).launch()()
            }
        })

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<ParseFailure, Command> {
        val lang = context.event.getOption(context.config.language.container.languageCommandOptionCode)?.asString?.uppercase()?.let {
            matchLang(it)
        } ?: return this.composeMissMatchFailure(context.channel, context.user)

        return Either.Right(LangCommand(lang))
    }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<ParseFailure, Command> {
        val lang = payload
            .getOrNull(1)
            ?.uppercase()
            ?.let { matchLang(it) }
            ?: return this.composeMissMatchFailure(context.channel, context.user)

        return Either.Right(LangCommand(lang))
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.languageCommand,
            container.languageCommandDescription,
        ) {
            option<String>(
                container.languageCommandOptionCode,
                container.languageCommandOptionCodeDescription,
                true
            ) {
                Language.entries.fold(this) { builder, language ->
                    builder.choice(
                        language.container.languageCode,
                        language.container.languageCode
                    )
                }
            }
        }

}
