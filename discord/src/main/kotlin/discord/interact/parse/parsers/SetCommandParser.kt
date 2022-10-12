package discord.interact.parse.parsers

import core.assets.Notation
import core.assets.User
import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.ResponseFlag
import core.interact.commands.SetCommand
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageAdaptor
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.GameManager
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
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import renju.notation.Pos
import renju.notation.Renju
import utils.lang.pair
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

    private fun buildAppendMessageProcedure(maybeMessage: Option<MessageAdaptor<Message, DiscordComponents>>, context: InteractionContext<*>, session: GameSession): IO<List<Order>> =
        maybeMessage.fold(
            onDefined = { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef); emptyList() } },
            onEmpty = { IO { emptyList() } }
        )

    private fun buildOrderFailure(context: InteractionContext<*>, session: GameSession, player: User): DiscordParseFailure =
        this.asParseFailure("try move but now $player's turn", context.guild, context.user) { producer, publisher, container ->
            producer.produceOrderFailure(publisher, container, player)
                .retrieve()
                .flatMap { this.buildAppendMessageProcedure(it, context, session) }
        }

    private fun buildMissMatchFailure(context: InteractionContext<*>, session: GameSession): DiscordParseFailure =
        this.asParseFailure("try move but argument mismatch", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetIllegalArgument(publisher, container)
                .retrieve()
                .flatMap { this.buildAppendMessageProcedure(it, context, session) }
        }

    private fun buildExistFailure(context: InteractionContext<*>, session: GameSession, pos: Pos): DiscordParseFailure =
        this.asParseFailure("make move but already exist", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetAlreadyExist(publisher, container, pos)
                .retrieve()
                .flatMap { this.buildAppendMessageProcedure(it, context, session) }
        }

    private fun buildForbiddenMoveFailure(context: InteractionContext<*>, session: GameSession, pos: Pos, flag: Byte): DiscordParseFailure =
        this.asParseFailure("make move but forbidden", context.guild, context.user) { producer, publisher, container ->
            producer.produceSetForbiddenMove(publisher, container, pos, flag)
                .retrieve()
                .flatMap { this.buildAppendMessageProcedure(it, context, session) }
        }

    private suspend fun parseRawCommand(context: InteractionContext<*>, user: User, rawRow: String?, rawColumn: String?): Either<Command, DiscordParseFailure> =
        this.retrieveSession(context.bot, context.guild, user).flatMapLeft { session ->
            if (session.player.id != user.id)
                return@flatMapLeft Either.Right(this.buildOrderFailure(context, session, session.player))

            if (rawRow == null || rawColumn == null)
                return@flatMapLeft Either.Right(buildMissMatchFailure(context, session))

            val row = this.matchRow(rawRow)
            val column = this.matchColumn(rawColumn.lowercase())

            if (row == null || column == null)
                return@flatMapLeft Either.Right(buildMissMatchFailure(context, session))

            val pos = Pos(row, column)

            val ref = when (context.config.sweepPolicy) {
                SweepPolicy.EDIT -> SessionManager.viewHeadMessage(context.bot.sessions, session.messageBufferKey)
                else -> null
            }

            GameManager.validateMove(session, pos)
                .flatMap { invalidKind ->
                    when (invalidKind) {
                        Notation.InvalidKind.Exist -> Option(this.buildExistFailure(context, session, pos))
                        Notation.InvalidKind.Forbidden -> when(session.board.nextColor()) {
                            Notation.Color.Black -> Option(this.buildForbiddenMoveFailure(context, session, pos, session.board.field()[pos.idx()]))
                            else -> Option.Empty
                        }
                        else -> throw IllegalStateException()
                    }
                }
                .fold(
                    onDefined = { Either.Right(it) },
                    onEmpty = {
                        val responseFlag = when (context.config.sweepPolicy) {
                            SweepPolicy.EDIT -> ResponseFlag.DeferWindowed
                            else -> ResponseFlag.Defer
                        }

                        Either.Left(SetCommand(session, pos, ref, responseFlag))
                    }
                )
        }

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val rawColumn = context.event.getOption(context.config.language.container.setCommandOptionColumn())?.asString
        val rawRow = context.event.getOption(context.config.language.container.setCommandOptionRow())?.asString

        return this.parseRawCommand(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        val (rawColumn, rawRow) = payload
            .drop(1)
            .take(2)
            .takeIf { it.size == 2 }
            ?.let { it.component1() pair it.component2() }
            ?: (null pair null)

        return this.parseRawCommand(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (column, row) = context.event.componentId
            .drop(2)
            .let { this.matchColumn(it.take(1)) pair this.matchRow(it.drop(1)) }

        if (row == null || column == null) return Option.Empty

        val pos = Pos(row, column)

        val userId = context.user.id

        val session = SessionManager.retrieveGameSession(context.bot.sessions, context.guild, userId)
            ?: return Option.Empty

        if (session.player.id != userId)
            return Option.Empty

        return Option(SetCommand(session, pos, null, ResponseFlag.Defer(context.config.sweepPolicy == SweepPolicy.EDIT)))
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
