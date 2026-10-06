package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.buildBoardInput
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.*
import renju.notation.Pos
import kotlin.time.Instant

enum class FocusDirection {
    LEFT, DOWN, UP, RIGHT, CENTER
}

class FocusCommand(
    private val sessionId: SessionId,
    private val direction: FocusDirection,
) : Command {

    override val name = "focus"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).interact<Effect<Nothing, Unit>> { runtime ->
            val session = runtime.session
            check(session.users.black.id == user.id || session.users.white.id == user.id)
            if (session !is PlayGameSession && session !is MoveStageOpeningSession &&
                session !is OfferStageOpeningSession && session !is SelectStageOpeningSession) return@interact effect { }

            val navigation = runtime.boardNavigation ?: return@interact effect { }
            val messageRef = runtime.messageRef ?: return@interact effect { }

            val step = service.focusWidth / 2 + 1
            val row = navigation.focus.row
            val col = navigation.focus.col

            val focus = when (this.direction) {
                FocusDirection.LEFT -> Pos(row, (col - step).coerceIn(service.focusRange))
                FocusDirection.DOWN -> Pos((row - step).coerceIn(service.focusRange), col)
                FocusDirection.UP -> Pos((row + step).coerceIn(service.focusRange), col)
                FocusDirection.RIGHT -> Pos(row, (col + step).coerceIn(service.focusRange))
                FocusDirection.CENTER -> navigation.initialFocus.focus
            }
            if (focus == navigation.focus) return@interact effect { }

            runtime.boardNavigation = navigation.copy(focus = focus)
            effect {
                service.updateInputBoard(messageRef, session.buildBoardInput(navigation.initialFocus.copy(focus = focus)))
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "move focus ${this.direction}", channel, user))
    }

}
