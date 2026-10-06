package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.effect
import core.assets.User
import core.assets.forbiddenKindToText
import core.database.DatabaseConnection
import core.interact.commands.*
import core.interact.i18n.LanguageContainer
import core.interact.message.AppMessage
import core.interact.parse.ParseFailure
import core.interact.parse.SessionSideParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.*
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
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

object SetCommandParser : SessionSideParser(), ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "s"

    override fun getLocalizedName(container: LanguageContainer) = "s"

    override fun getLocalizedUsages(container: LanguageContainer): List<BuildableCommand.Usage> = emptyList()

    private fun buildOrderFailure(context: UserInteractionContext<*>, player: User): ParseFailure =
        this.asParseFailure("try move but now $player's turn", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                publisher(AppMessage.Text(container.processErrorOrder(messagingService.formatUser(player)))).retrieve()()
            }
        }

    private fun buildMissMatchFailure(context: UserInteractionContext<*>): ParseFailure =
        this.asParseFailure("try move but argument mismatch", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                publisher(AppMessage.Text(container.setErrorIllegalArgument)).retrieve()()
            }
        }

    private fun buildExistFailure(context: UserInteractionContext<*>, pos: Pos): ParseFailure =
        this.asParseFailure("make move but already exist", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                publisher(AppMessage.Text(container.setErrorExist(messagingService.formatHighlight(pos.toString())))).retrieve()()
            }
        }

    private fun buildForbiddenMoveFailure(context: UserInteractionContext<*>, pos: Pos, forbiddenKind: ForbiddenKind?): ParseFailure =
        this.asParseFailure("make move but forbidden", context.channel, context.user) { messagingService, publisher, container ->
            effect {
                val notice = container.setErrorForbidden(
                    messagingService.formatHighlight(pos.toString()),
                    messagingService.formatHighlight(forbiddenKindToText(forbiddenKind)),
                )
                publisher(AppMessage.Text(notice)).retrieve()()
            }
        }

    private fun buildSilentFailure(context: UserInteractionContext<*>): ParseFailure =
        this.asParseFailure("unknown error", context.channel, context.user) { _, _, _ ->
            effect { }
        }

    private fun branchCommandBySession(sessionId: SessionId, session: GameSession, pos: Pos, responseFlag: ResponseFlag): Command? =
        when (session) {
            is PlayGameSession -> PlayCommand(sessionId, pos, responseFlag)
            is MoveStageOpeningSession -> OpeningSetCommand(sessionId, pos, responseFlag)
            is OfferStageOpeningSession -> OpeningOfferCommand(sessionId, pos, responseFlag)
            is SelectStageOpeningSession -> OpeningSelectCommand(sessionId, pos, responseFlag)
            else -> null
        }

    context(sessions: SessionPool)
    private fun parseRawCommand(context: UserInteractionContext<*>, user: User.Human, rawPosition: String?): Either<ParseFailure, Command> =
        this.retrieveSession(context.channel, user).flatMap { (sessionId, session) ->
            if (session.player.id != user.id)
                return@flatMap Either.Left(this.buildOrderFailure(context, session.player))

            val pos = rawPosition?.let { Pos.fromCartesian(it) }
                ?: return@flatMap Either.Left(this.buildMissMatchFailure(context))

            val failure = when (session.state.board.validateMove(pos)) {
                MoveError.Exist -> this.buildExistFailure(context, pos)
                MoveError.Forbidden -> when (session.state.board.playerColor) {
                    Color.BLACK -> this.buildForbiddenMoveFailure(context, pos, session.state.board.forbiddenKind(pos))
                    else -> null
                }
                null -> null
            }

            if (failure != null) {
                Either.Left(failure)
            } else {
                this.branchCommandBySession(sessionId, session, pos, ResponseFlag.DeferWindowed)
                    ?.let { Either.Right(it) }
                    ?: Either.Left(this.buildSilentFailure(context))
            }
        }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<ParseFailure, Command> {
        val rawPosition = context.event.getOption(context.config.language.container.setCommandOptionPosition)?.asString

        return this.parseRawCommand(context, context.user, rawPosition)
    }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<ParseFailure, Command> {
        val rawPosition = payload
            .drop(1)
            .singleOrNull()

        return this.parseRawCommand(context, context.user, rawPosition)
    }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val pos = context.event.componentId
            .drop(2)
            .let { Pos.fromCartesian(it) }
            ?: return null

        val userId = context.user.id

        val sessionId = SessionManager.findGameSessionId(context.channel.id, userId)
            ?: return null
        val session = SessionManager.retrieveGameSession(sessionId).snapshot()

        if (session.player.id != userId)
            return null

        if (!session.isLegalMove(pos)) {
            return null
        }

        return this.branchCommandBySession(sessionId, session, pos, ResponseFlag.DeferEdit)
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            "s",
            container.setCommandDescription,
        ) {
            option<String>(container.setCommandOptionPosition, container.setCommandOptionPositionDescription,
                required = true,
                autocomplete = false
            )
        }

}
