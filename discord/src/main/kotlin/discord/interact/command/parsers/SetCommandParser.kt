package discord.interact.command.parsers

import core.assets.Order
import core.assets.User
import core.interact.commands.Command
import core.interact.commands.SetCommand
import core.interact.i18n.LanguageContainer
import core.session.GameManager
import core.session.RejectReason
import core.session.SessionManager
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractId
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.command.*
import discord.interact.message.DiscordMessageBinder
import jrenju.notations.Pos
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either
import utils.monads.Option

object SetCommandParser : ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    private fun matchColumn(option: String): Int? =
        option.first().uppercase().firstOrNull()?.digitToInt()?.let { if (it in 65..79) it - 65 else null }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()?.let { if (it in 1..15) it else null }

    private fun composeMissMatchFailure(container: LanguageContainer, user: User) =
        this.asParseFailure("$user try move but argument missmatch") { _, publisher ->
            DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT } // TODO
        }

    private fun composeSessionNotFoundFailure(container: LanguageContainer, user: User) =
        this.asParseFailure("$user session not found") { _, publisher ->
            DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT } // TODO
        }

    private fun composeExistFailure(container: LanguageContainer, user: User) =
        this.asParseFailure("$user make move but already exist") { _, publisher ->
            DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT } // TODO
        }

    private fun composeForbiddenMoveFailure(container: LanguageContainer, user: User) =
        this.asParseFailure("$user make move but forbidden") { _, publisher ->
            DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT } // TODO
        }

    private suspend fun parseActually(context: InteractionContext<*>, user: User, rawColumn: String?, rawRow: String?): Either<Command, ParseFailure> {
        val session = SessionManager.retrieveGameSession(context.botContext.sessionRepository, context.config.id, user.id)
            ?: return Either.Right(composeSessionNotFoundFailure(context.config.language.container, user))

        if (rawColumn == null || rawRow == null) return Either.Right(composeMissMatchFailure(context.config.language.container, user))

        val column = matchColumn(rawColumn)
        val row = matchRow(rawRow)

        if (column == null || row == null) return Either.Right(composeMissMatchFailure(context.config.language.container, user))

        val pos = Pos(column, row)

        return GameManager.validateMove(session, pos).fold(
            onDefined = { when (it) {
                RejectReason.EXIST -> Either.Right(this.composeExistFailure(context.config.language.container, user))
                RejectReason.FORBIDDEN -> Either.Right(this.composeForbiddenMoveFailure(context.config.language.container, user))
            } },
            onEmpty = { Either.Left(SetCommand("s", pos)) }
        )
    }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, ParseFailure> {
        val rawColumn = context.event.getOption("row")?.asString
        val rawRow = context.event.getOption("column")?.asString

        return this.parseActually(context, context.event.user.extractUser(), rawColumn, rawRow)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, ParseFailure> {
        val (rawColumn, rawRow) = context.event.message.contentRaw
            .drop("s".length + 2)
            .split(" ")
            .take(2)

        return this.parseActually(context, context.event.author.extractUser(), rawColumn, rawRow)
    }

    override suspend fun parseButton(context: InteractionContext<ButtonInteractionEvent>): Option<Command> {
        val (column, row) = context.event.componentId
            .drop(2)
            .let { this.matchColumn(it.take(1)) to this.matchRow(it.drop(1)) }

        if (column == null || row == null) return Option.Empty

        val pos = Pos(column, row)

        val session = SessionManager.retrieveGameSession(context.botContext.sessionRepository, context.config.id, context.event.user.extractId())
            ?: return Option.Empty

        if (session.board.validateMove(pos.idx()).isDefined) return Option.Empty

        return Option.Some(SetCommand("s", pos))
    }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            "s",
            "set"
        ) {
            option<String>("row", "alphabet", true)
            option<Int>("column", "number", true)
        }

}
