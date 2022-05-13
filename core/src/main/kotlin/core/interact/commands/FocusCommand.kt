package core.interact.commands

import core.BotContext
import core.assets.User
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.Deferred
import utils.structs.IO

enum class Direction {
    LEFT, DOWN, UP, RIGHT, FOCUS
}

class FocusCommand(override val command: String, private val session: GameSession, private val direction: Direction) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        val originalMessage = message.await()

        val focusState = SessionManager.getNavigateState(bot.sessionRepository, originalMessage.message)!!

        val newFocus = run {
            val kernelHalf = producer.focusWidth / 2
            val step = kernelHalf + 1

            val row = Pos.idxToRow(focusState.page)
            val col = Pos.idxToCol(focusState.page)

            when (this.direction) {
                Direction.LEFT -> Pos(
                    row,
                    (col - step).coerceIn(kernelHalf, Renju.BOARD_WIDTH_MAX_IDX() - kernelHalf)
                )
                Direction.DOWN -> Pos(
                    (row - step).coerceIn(kernelHalf, Renju.BOARD_WIDTH_MAX_IDX() - kernelHalf),
                    col
                )
                Direction.UP -> Pos(
                    (row + step).coerceIn(kernelHalf, Renju.BOARD_WIDTH_MAX_IDX() - kernelHalf),
                    col
                )
                Direction.RIGHT -> Pos(
                    row,
                    (col + step).coerceIn(kernelHalf, Renju.BOARD_WIDTH_MAX_IDX() - kernelHalf)
                )
                Direction.FOCUS -> FocusSolver.resolveFocus(this.session.board, producer.focusWidth)
            }
        }

        SessionManager.addNavigate(bot.sessionRepository, originalMessage.message, focusState.copy(page = newFocus.idx()))

        val action = originalMessage.updateButtons(
            producer.generateFocusedButtons(
                producer.generateFocusedField(this.session.board, newFocus)
            )
        )

        IO { action.launch(); Order.Unit } to this.asCommandReport("succeed", user)
    }

}
