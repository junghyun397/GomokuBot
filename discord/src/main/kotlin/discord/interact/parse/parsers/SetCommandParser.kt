package discord.interact.parse.parsers

import core.interact.Order
import core.assets.User
import core.interact.commands.Command
import core.interact.commands.SetCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.GameManager
import core.session.RejectReason
import core.session.SessionManager
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractId
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.message.DiscordButtons
import discord.interact.parse.*
import discord.interact.message.DiscordMessageProducer
import jrenju.notation.Pos
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.Option
import utils.structs.flatMapLeft

object SetCommandParser : SessionSideParser<Message, DiscordButtons>(), ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    private fun matchColumn(option: String): Int? =
        option.first().uppercase().firstOrNull()
            ?.digitToInt()
            ?.let { if (it in 65..79) it - 65 else null }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()?.let { if (it in 1..15) it else null }

    private fun composeMissMatchFailure(container: LanguageContainer, user: User): DiscordParseFailure =
        this.asParseFailure("$user try move but argument missmatch") { _, publisher, _ ->
            DiscordMessageProducer.produceLanguageGuide(publisher).map { it.launch(); Order.Unit } // TODO
        }

    private fun composeExistFailure(container: LanguageContainer, user: User): DiscordParseFailure =
        this.asParseFailure("$user make move but already exist") { _, publisher, _ ->
            DiscordMessageProducer.produceLanguageGuide(publisher).map { it.launch(); Order.Unit } // TODO
        }

    private fun composeForbiddenMoveFailure(container: LanguageContainer, user: User): DiscordParseFailure =
        this.asParseFailure("$user make move but forbidden") { _, publisher, _ ->
            DiscordMessageProducer.produceLanguageGuide(publisher).map { it.launch(); Order.Unit } // TODO
        }

    private suspend fun parseActually(context: InteractionContext<*>, user: User, rawColumn: String?, rawRow: String?): Either<Command, DiscordParseFailure> =
        this.retrieveSessionWith(context.botContext, context.guild, user).flatMapLeft { session ->
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
                onEmpty = { Either.Left(SetCommand("s", session, pos)) }
            )
        }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val rawColumn = context.event.getOption("row")?.asString
        val rawRow = context.event.getOption("column")?.asString

        return this.parseActually(context, context.event.user.extractUser(), rawColumn, rawRow)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>): Either<Command, DiscordParseFailure> {
        val (rawColumn, rawRow) = context.event.message.contentRaw
            .drop(2)
            .split(" ")
            .take(2)
            .let { if (it.size != 2) listOf(null, null) else it }

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

        return Option.Some(SetCommand("s", session, pos))
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
