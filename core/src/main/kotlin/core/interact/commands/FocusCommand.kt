package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.MessageManager
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.ChannelConfig
import core.session.entities.SessionId
import renju.notation.Pos
import kotlin.time.Instant

enum class Direction {
    LEFT, DOWN, UP, RIGHT, CENTER
}

class FocusCommand(
    private val navigationState: BoardNavigationState,
    private val sessionId: SessionId,
    private val direction: Direction,
    private val messageRef: MessageRef,
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
        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).snapshot()
        val newFocus = run {
            val step = service.focusWidth / 2 + 1

            val row = Pos.idxToRow(this.navigationState.page)
            val col = Pos.idxToCol(this.navigationState.page)

            when (this.direction) {
                Direction.LEFT -> Pos(row, (col - step).coerceIn(service.focusRange))
                Direction.DOWN -> Pos((row - step).coerceIn(service.focusRange), col)
                Direction.UP -> Pos((row + step).coerceIn(service.focusRange), col)
                Direction.RIGHT -> Pos(row, (col + step).coerceIn(service.focusRange))
                Direction.CENTER -> this.navigationState.focusInfo.focus
            }
        }

        val newFocusInfo = this.navigationState.focusInfo.copy(focus = newFocus)

        when (newFocus.idx) {
            this.navigationState.page -> CommandResult(effect { }, this.writeActionLog(emittedTime, "focus bounded", channel, user))
            else -> {
                MessageManager.addNavigation(bot.sessions, this.messageRef, this.navigationState.copy(page = newFocus.idx))

                val action = effect {
                    service.upsertInputBoard(publishers.component, service.generateFocusedField(session, newFocusInfo))
                        .launch()()
                }

                CommandResult(action, this.writeActionLog(emittedTime, "move focus ${this.direction}", channel, user))
            }
        }
    }

}
