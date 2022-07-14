package core.interact.commands

import core.BotContext
import core.assets.Guild
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
import core.session.entities.NavigateState
import jrenju.notation.Pos
import kotlinx.coroutines.Deferred
import utils.structs.IO

enum class Direction {
    LEFT, DOWN, UP, RIGHT, FOCUS
}

class FocusCommand(
    override val command: String,
    private val navigateState: NavigateState,
    private val session: GameSession,
    private val direction: Direction,
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>
    ) = runCatching {
        val originalMessage = message.await()

        val newFocus = run {
            val step = producer.focusWidth / 2 + 1

            val row = Pos.idxToRow(this.navigateState.page)
            val col = Pos.idxToCol(this.navigateState.page)

            when (this.direction) {
                Direction.LEFT -> Pos(row, (col - step).coerceIn(producer.focusRange))
                Direction.DOWN -> Pos((row - step).coerceIn(producer.focusRange), col)
                Direction.UP -> Pos((row + step).coerceIn(producer.focusRange), col)
                Direction.RIGHT -> Pos(row, (col + step).coerceIn(producer.focusRange))
                Direction.FOCUS -> FocusSolver.resolveFocus(this.session.board, producer.focusWidth)
            }
        }

        SessionManager.addNavigate(bot.sessions, originalMessage.messageRef, this.navigateState.copy(page = newFocus.idx()))

//        return@runCatching producer.produceBoard(editPublisher, config.language.container, config.boardStyle.renderer, this.session)
//            .map { producer.attachFocusButtons(it, this.session, newFocus).launch(); emptyList<Order>() } to this.asCommandReport("move focus $direction", user)

        val action = originalMessage.updateButtons(
            producer.generateFocusedButtons(producer.generateFocusedField(this.session.board, newFocus))
        )

        IO { action.launch(); emptyList<Order>() } to this.asCommandReport("move focus $direction", user)
    }

}
