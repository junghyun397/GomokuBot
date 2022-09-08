package discord.interact.parse.parsers

import core.assets.User
import core.interact.commands.Command
import core.interact.commands.ResponseFlag
import core.interact.commands.SetCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.GameManager
import core.session.RejectReason
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.GameSession
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.InteractionContext
import discord.interact.message.DiscordComponents
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import jrenju.notation.Color
import jrenju.notation.Pos
import jrenju.notation.Renju
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.lang.and
import utils.structs.*

object SetCommandParser : SessionSideParser<Message, DiscordComponents>(), ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    private fun matchColumn(option: String): Int? =
        option.firstOrNull()
            ?.takeIf { it.code in 97 .. 97 + Renju.BOARD_WIDTH() }
            ?.let { it.code - 97 }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()
            ?.takeIf { it in 1 .. Renju.BOARD_WIDTH() }
            ?.let { it - 1 }

    private fun composeOrderFailure(context: InteractionContext<*>, session: GameSession, player: User): DiscordParseFailure =
        this.asParseFailure("try move but now $player's turn", context.guild, context.user) { producer, publisher, container ->
            producer.produceOrderFailure(publisher, container, player)
                .retrieve()
                .flatMapOption { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) } }
                .map { emptyList() }
        }

    private fun composeMissMatchFailure(context: InteractionContext<*>, session: GameSession): DiscordParseFailure =
        this.asParseFailure("try move but argument mismatch", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetIllegalArgument(publisher, container)
                .retrieve()
                .flatMapOption { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) } }
                .map { emptyList() }
        }

    private fun composeExistFailure(context: InteractionContext<*>, session: GameSession, pos: Pos): DiscordParseFailure =
        this.asParseFailure("make move but already exist", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetAlreadyExist(publisher, container, pos)
                .retrieve()
                .flatMapOption { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) } }
                .map { emptyList() }
        }

    private fun composeForbiddenMoveFailure(context: InteractionContext<*>, session: GameSession, pos: Pos, flag: Byte): DiscordParseFailure =
        this.asParseFailure("make move but forbidden", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetForbiddenMove(publisher, container, pos, flag)
                .retrieve()
                .flatMapOption { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) } }
                .map { emptyList() }
        }

    private suspend fun parseActually(context: InteractionContext<*>, user: User, rawRow: String?, rawColumn: String?): Either<Command, DiscordParseFailure> =
        this.retrieveSession(context.bot, context.guild, user).flatMapLeft { session ->
            if (session.player.id != user.id)
                return Either.Right(this.composeOrderFailure(context, session, session.player))

            if (rawRow == null || rawColumn == null)
                return Either.Right(composeMissMatchFailure(context, session))

            val row = this.matchRow(rawRow)
            val column = this.matchColumn(rawColumn.lowercase())

            if (row == null || column == null)
                return Either.Right(composeMissMatchFailure(context, session))

            val pos = Pos(row, column)

            return GameManager.validateMove(session, pos).fold(
                onDefined = { when (it) {
                    RejectReason.EXIST -> Either.Right(this.composeExistFailure(context, session, pos))
                    RejectReason.FORBIDDEN ->
                        if (session.board.nextColor() == Color.BLACK())
                            Either.Right(this.composeForbiddenMoveFailure(context, session, pos, session.board.boardField()[pos.idx()]))
                        else
                            Either.Left(SetCommand(session, pos, ResponseFlag.Defer()))
                } },
                onEmpty = { Either.Left(SetCommand(session, pos, ResponseFlag.Defer())) }
            )
        }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val rawColumn = context.event.getOption(context.config.language.container.setCommandOptionColumn())?.asString
        val rawRow = context.event.getOption(context.config.language.container.setCommandOptionRow())?.asString

        return this.parseActually(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        val (rawColumn, rawRow) = payload
            .drop(1)
            .take(2)
            .takeIf { it.size == 2 }
            ?.let { it.component1() and it.component2() }
            ?: (null and null)

        return this.parseActually(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (column, row) = context.event.componentId
            .drop(2)
            .let { this.matchColumn(it.take(1)) and this.matchRow(it.drop(1)) }

        if (row == null || column == null) return Option.Empty

        val pos = Pos(row, column)

        val userId = context.user.id

        val session = SessionManager.retrieveGameSession(context.bot.sessions, context.guild, userId)
            ?: return Option.Empty

        if (session.player.id != userId)
            return Option.Empty

        return Option(SetCommand(session, pos, ResponseFlag.Defer(context.config.sweepPolicy == SweepPolicy.EDIT)))
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            "s",
            container.setCommandDescription(),
        ) {
            option<String>(container.setCommandOptionColumn(), container.setCommandOptionColumnDescription(), true)
            option<Int>(container.setCommandOptionRow(), container.setCommandOptionRowDescription(), true)
        }

}
