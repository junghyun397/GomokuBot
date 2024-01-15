package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import renju.notation.Pos
import utils.lang.tuple
import utils.structs.IO
import utils.structs.map

enum class Direction {
    LEFT, DOWN, UP, RIGHT, CENTER
}

class FocusCommand(
    private val navigationState: BoardNavigationState,
    private val session: GameSession,
    private val direction: Direction,
) : Command {

    override val name = "focus"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
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

        when(newFocus.idx()) {
            this.navigationState.page -> tuple(IO.value(emptyOrders), this.writeCommandReport("focus bounded", guild, user))
            else -> {
                SessionManager.addNavigation(bot.sessions, messageRef, this.navigationState.copy(page = newFocus.idx()))

                val action = service.dispatchFocusButtons(publishers.component, service.generateFocusedField(session, newFocusInfo))
                    .launch()
                    .map { emptyOrders }

                tuple(action, this.writeCommandReport("move focus $direction", guild, user))
            }
        }
    }

}
