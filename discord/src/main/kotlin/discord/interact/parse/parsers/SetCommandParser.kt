package discord.interact.parse.parsers

import core.assets.User
import core.interact.Order
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
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
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
        option.first()
            .digitToInt()
            .let { if (it in 65..79) it - 65 else null }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()?.let { if (it in 1..15) it else null }

    private fun composeMissMatchFailure(user: User): DiscordParseFailure =
        this.asParseFailure("try move but argument missmatch", user) { producer, publisher, container ->
            producer.produceSetIllegalArgument(publisher, container).map { it.launch(); Order.Unit }
        }

    private fun composeExistFailure(user: User, pos: Pos): DiscordParseFailure =
        this.asParseFailure("make move but already exist", user) { producer, publisher, container ->
            producer.produceSetAlreadyExist(publisher, container, user, pos).map { it.launch(); Order.Unit }
        }

    private fun composeForbiddenMoveFailure(user: User, pos: Pos, flag: Byte): DiscordParseFailure =
        this.asParseFailure("make move but forbidden", user) { producer, publisher, container ->
            producer.produceSetForbiddenMove(publisher, container, user, pos, flag).map { it.launch(); Order.Unit }
        }

    private suspend fun parseActually(context: InteractionContext<*>, user: User, rawColumn: String?, rawRow: String?): Either<Command, DiscordParseFailure> =
        this.retrieveSession(context.botContext, context.guild, user).flatMapLeft { session ->
            if (rawColumn == null || rawRow == null) return Either.Right(composeMissMatchFailure(user))

            val column = matchColumn(rawColumn)
            val row = matchRow(rawRow)

            if (column == null || row == null) return Either.Right(composeMissMatchFailure(user))

            val pos = Pos(column, row)

            return GameManager.validateMove(session, pos).fold(
                onDefined = { when (it) {
                    RejectReason.EXIST -> Either.Right(this.composeExistFailure(user, pos))
                    RejectReason.FORBIDDEN -> Either.Right(this.composeForbiddenMoveFailure(user, pos, session.board.boardField()[pos.idx()]))
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
