package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import jrenju.notation.Pos
import utils.lang.and
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
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val newFocus = run {
            val step = producer.focusWidth / 2 + 1

            val row = Pos.idxToRow(this.navigationState.page)
            val col = Pos.idxToCol(this.navigationState.page)

            when (this.direction) {
                Direction.LEFT -> Pos(row, (col - step).coerceIn(producer.focusRange))
                Direction.DOWN -> Pos((row - step).coerceIn(producer.focusRange), col)
                Direction.UP -> Pos((row + step).coerceIn(producer.focusRange), col)
                Direction.RIGHT -> Pos(row, (col + step).coerceIn(producer.focusRange))
                Direction.CENTER -> FocusSolver.resolveCenter(this.session.board, producer.focusRange)
            }
        }

        when(newFocus.idx()) {
            this.navigationState.page -> IO { emptyList<Order>() } and this.asCommandReport("focus bounded", guild, user)
            else -> {
                SessionManager.addNavigate(bot.sessions, messageRef, this.navigationState.copy(page = newFocus.idx()))

                val action = producer.attachFocusButtons(publishers.component, session, newFocus)
                    .launch()
                    .map { emptyList<Order>() }

                action and this.asCommandReport("move focus $direction", guild, user)
            }
        }
    }

}
