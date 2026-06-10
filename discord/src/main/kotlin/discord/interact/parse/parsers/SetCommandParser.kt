package discord.interact.parse.parsers

import core.session.MessageManager
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.MessageRef
import core.assets.User
import core.assets.humanId
import core.interact.Order
import core.interact.commands.*
import core.interact.emptyOrders
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageAdaptor
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordComponents
import discord.interact.message.DiscordMessageData
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import renju.MoveError
import renju.notation.Color
import renju.notation.ForbiddenKind
import renju.notation.Pos
import utils.lang.tuple

object SetCommandParser : SessionSideParser<DiscordMessageData, DiscordComponents>(), ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    override fun getLocalizedName(container: LanguageContainer) = "s"

    override fun getLocalizedUsages(container: LanguageContainer): List<BuildableCommand.Usage> = emptyList()

    private fun matchColumn(option: String): Int? =
        option.firstOrNull()
            ?.takeIf { it.code in 97 .. 97 + Pos.BOARD_WIDTH }
            ?.let { it.code - 97 }

    private fun matchRow(option: String): Int? =
        option.toIntOrNull()
            ?.takeIf { it in 1 .. Pos.BOARD_WIDTH }
            ?.let { it - 1 }

    private fun buildAppendMessageProcedure(maybeMessage: MessageAdaptor<DiscordMessageData, DiscordComponents>?, context: UserInteractionContext<*>, session: GameSession): Effect<Nothing, List<Order>> =
        effect {
            if (maybeMessage != null) {
                MessageManager.appendMessage(context.bot.sessions, session.messageBufferKey, maybeMessage.messageRef)
                emptyList()
            } else {
                emptyOrders
            }
        }

    private fun buildOrderFailure(context: UserInteractionContext<*>, session: GameSession, player: User): DiscordParseFailure =
        this.asParseFailure("try move but now $player's turn", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                val maybeMessage = messagingService.buildSetOrderFailure(publisher, container, player).retrieve()()
                this@SetCommandParser.buildAppendMessageProcedure(maybeMessage, context, session)()
            }
        }

    private fun buildMissMatchFailure(context: UserInteractionContext<*>, session: GameSession): DiscordParseFailure =
        this.asParseFailure("try move but argument mismatch", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                val maybeMessage = messagingService.buildSetIllegalArgumentFailure(publisher, container).retrieve()()
                this@SetCommandParser.buildAppendMessageProcedure(maybeMessage, context, session)()
            }
        }

    private fun buildExistFailure(context: UserInteractionContext<*>, session: GameSession, pos: Pos): DiscordParseFailure =
        this.asParseFailure("make move but already exist", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                val maybeMessage = messagingService.buildSetAlreadyExistFailure(publisher, container, pos).retrieve()()
                this@SetCommandParser.buildAppendMessageProcedure(maybeMessage, context, session)()
            }
        }

    private fun buildForbiddenMoveFailure(context: UserInteractionContext<*>, session: GameSession, pos: Pos, forbiddenKind: ForbiddenKind?): DiscordParseFailure =
        this.asParseFailure("make move but forbidden", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                val maybeMessage = messagingService.buildSetForbiddenMoveFailure(publisher, container, pos, forbiddenKind).retrieve()()
                this@SetCommandParser.buildAppendMessageProcedure(maybeMessage, context, session)()
            }
        }

    private fun buildSilentFailure(context: UserInteractionContext<*>): DiscordParseFailure =
        this.asParseFailure("unknown error", context.channel, context.user) { _, _, _ ->
            effect { emptyOrders }
        }

    private fun branchCommandBySession(sessionId: SessionId, session: GameSession, pos: Pos, ref: MessageRef?, responseFlag: ResponseFlag): Command? =
        when (session) {
            is RenjuSession -> PlayCommand(sessionId, pos, ref, responseFlag)
            is MoveStageOpeningSession -> OpeningSetCommand(sessionId, pos, ref, responseFlag)
            is OfferStageOpeningSession -> OpeningOfferCommand(sessionId, pos, ref, responseFlag)
            is SelectStageOpeningSession -> OpeningSelectCommand(sessionId, pos, ref, responseFlag)
            else -> null
        }

    private suspend fun parseRawCommand(context: UserInteractionContext<*>, user: User.Human, rawRow: String?, rawColumn: String?): Either<DiscordParseFailure, Command> =
        this.retrieveSession(context.bot, context.channel, user).flatMap { (sessionId, session) ->
            if (session.player.humanId != user.id)
                return@flatMap Either.Left(this.buildOrderFailure(context, session, session.player))

            if (rawRow == null || rawColumn == null)
                return@flatMap Either.Left(buildMissMatchFailure(context, session))

            val row = this.matchRow(rawRow)
            val column = this.matchColumn(rawColumn.lowercase())

            if (row == null || column == null)
                return@flatMap Either.Left(buildMissMatchFailure(context, session))

            val pos = Pos(row, column)

            val ref = when (context.config.swapType) {
                SwapType.EDIT -> MessageManager.viewHeadMessage(context.bot.sessions, session.messageBufferKey)
                else -> null
            }

            val failure = when (GameManager.validateMove(session, pos)) {
                MoveError.Exist -> this.buildExistFailure(context, session, pos)
                MoveError.Forbidden -> when (session.board.playerColor) {
                    Color.Black -> this.buildForbiddenMoveFailure(context, session, pos, session.board.forbiddenKind(pos))
                    else -> null
                }
                null -> null
            }

            if (failure != null) {
                Either.Left(failure)
            } else {
                val responseFlag = when (context.config.swapType) {
                    SwapType.EDIT -> ResponseFlag.DeferWindowed
                    else -> ResponseFlag.Defer
                }

                this.branchCommandBySession(sessionId, session, pos, ref, responseFlag)
                    ?.let { Either.Right(it) }
                    ?: Either.Left(this.buildSilentFailure(context))
            }
        }

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command> {
        val rawColumn = context.event.getOption(context.config.language.container.setCommandOptionColumn())?.asString
        val rawRow = context.event.getOption(context.config.language.container.setCommandOptionRow())?.asString

        return this.parseRawCommand(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command> {
        val (rawColumn, rawRow) = payload
            .drop(1)
            .take(2)
            .takeIf { it.size == 2 }
            ?.let { tuple(it.component1(), it.component2()) }
            ?: tuple(null, null)

        return this.parseRawCommand(context, context.user, rawRow, rawColumn)
    }

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val (column, row) = context.event.componentId
            .drop(2)
            .let { tuple(this.matchColumn(it.take(1)), this.matchRow(it.drop(1))) }

        if (row == null || column == null) return null

        val pos = Pos(row, column)

        val userId = context.user.id

        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, userId)
            ?: return null
        val session = SessionManager.retrieveGameSession(context.bot.sessions, sessionId).snapshot()

        if (session.player.humanId != userId)
            return null

        if (GameManager.validateMove(session, pos) != null) {
            return null
        }

        return this.branchCommandBySession(sessionId, session, pos, null, ResponseFlag.Defer(context.config.swapType == SwapType.EDIT))
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
