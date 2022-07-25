package discord.interact.parse.parsers

import core.BotContext
import core.assets.User
import core.interact.commands.Command
import core.interact.commands.SetCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.GameManager
import core.session.RejectReason
import core.session.SessionManager
import core.session.entities.GameSession
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.InteractionContext
import discord.interact.message.DiscordButtons
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
import utils.structs.*

object SetCommandParser : SessionSideParser<Message, DiscordButtons>(), ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    private fun matchColumn(option: String): Int? =
        option.firstOrNull()?.let { if (it.code in 97 .. 97 + Renju.BOARD_WIDTH()) it.code - 97 else null }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()?.let { if (it in 1..Renju.BOARD_WIDTH()) it - 1 else null }

    private fun composeOrderFailure(context: BotContext, session: GameSession, user: User, player: User): DiscordParseFailure =
        this.asParseFailure("try move but now $player's turn", user) { producer, publisher, container ->
            producer.produceOrderFailure(publisher, container, user, player)
                .flatMap { IO { SessionManager.appendMessage(context.sessions, session.messageBufferKey, it.retrieve().messageRef) } }
                .map { emptyList() }
        }

    private fun composeMissMatchFailure(context: BotContext, session: GameSession, user: User): DiscordParseFailure =
        this.asParseFailure("try move but argument mismatch", user) { producer, publisher, container ->
            producer.produceSetIllegalArgument(publisher, container, user)
                .flatMap { IO { SessionManager.appendMessage(context.sessions, session.messageBufferKey, it.retrieve().messageRef) } }
                .map { emptyList() }
        }

    private fun composeExistFailure(context: BotContext, session: GameSession, user: User, pos: Pos): DiscordParseFailure =
        this.asParseFailure("make move but already exist", user) { producer, publisher, container ->
            producer.produceSetAlreadyExist(publisher, container, user, pos)
                .flatMap { IO { SessionManager.appendMessage(context.sessions, session.messageBufferKey, it.retrieve().messageRef) } }
                .map { emptyList() }
        }

    private fun composeForbiddenMoveFailure(context: BotContext, session: GameSession, user: User, pos: Pos, flag: Byte): DiscordParseFailure =
        this.asParseFailure("make move but forbidden", user) { producer, publisher, container ->
            producer.produceSetForbiddenMove(publisher, container, user, pos, flag)
                .flatMap { IO { SessionManager.appendMessage(context.sessions, session.messageBufferKey, it.retrieve().messageRef) } }
                .map { emptyList() }
        }

    private suspend fun parseActually(context: InteractionContext<*>, user: User, rawRow: String?, rawColumn: String?): Either<Command, DiscordParseFailure> =
        this.retrieveSession(context.bot, context.guild, user).flatMapLeft { session ->
            if (session.player.id != user.id)
                return Either.Right(this.composeOrderFailure(context.bot, session, user, session.player))

            if (rawRow == null || rawColumn == null)
                return Either.Right(composeMissMatchFailure(context.bot, session, user))

            val row = this.matchRow(rawRow)
            val column = this.matchColumn(rawColumn.lowercase())

            if (row == null || column == null)
                return Either.Right(composeMissMatchFailure(context.bot, session, user))

            val pos = Pos(row, column)

            return GameManager.validateMove(session, pos).fold(
                onDefined = { when (it) {
                    RejectReason.EXIST -> Either.Right(this.composeExistFailure(context.bot, session, user, pos))
                    RejectReason.FORBIDDEN ->
                        if (session.board.nextColor() == Color.BLACK())
                            Either.Right(this.composeForbiddenMoveFailure(context.bot, session, user, pos, session.board.boardField()[pos.idx()]))
                        else
                            Either.Left(SetCommand("s", session, pos))
                } },
                onEmpty = { Either.Left(SetCommand("s", session, pos)) }
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
            .let { if (it.size != 2) null to null else it.component1() to it.component2() }

        return this.parseActually(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (column, row) = context.event.componentId
            .drop(2)
            .let { this.matchColumn(it.take(1)) to this.matchRow(it.drop(1)) }

        if (row == null || column == null) return Option.Empty

        val pos = Pos(row, column)

        val userId = context.user.id

        val session = SessionManager.retrieveGameSession(context.bot.sessions, context.guild, userId)
            ?: return Option.Empty

        if (session.player.id != userId)
            return Option.Empty

        return Option(SetCommand("s", session, pos))
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
