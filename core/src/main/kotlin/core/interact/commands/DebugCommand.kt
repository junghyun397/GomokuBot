package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.inference.InferenceManager
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.message.ButtonFlag
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import jrenju.Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import utils.structs.Either
import utils.structs.IO

enum class DebugType {
    BOARD_DEMO, STATUS
}

class DebugCommand(override val command: String, private val debugType: DebugType, private val payload: String?) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching { when (debugType) {
        DebugType.BOARD_DEMO -> {
            val board = Board.newBoard()
                .makeMove(Pos(8, 7).idx())
                .makeMove(Pos(7, 6).idx())
                .calculateL2Board()
                .calculateL3Board()
                .calculateDeepL3Board()

            val focus = InferenceManager.resolveFocus(board, 5)

            val focusedFields = (-2 .. 2).map { colOff -> (-2 .. 2).map { rowOff ->
                val pos = Pos(focus.row() + rowOff, focus.col() + colOff)
                val flag = if (pos.idx() == board.latestMove()) when(board.boardField()[board.latestMove()]) {
                    Flag.BLACK() -> ButtonFlag.BLACK_RECENT
                    Flag.WHITE() -> ButtonFlag.WHITE_RECENT
                    else -> ButtonFlag.FREE
                } else when (board.boardField()[pos.idx()]) {
                    Flag.BLACK() -> ButtonFlag.BLACK
                    Flag.WHITE() -> ButtonFlag.WHITE
                    Flag.FORBIDDEN_33(), Flag.FORBIDDEN_44(), Flag.FORBIDDEN_6() -> ButtonFlag.FORBIDDEN
                    else -> ButtonFlag.FREE
                }
                "${(97 + pos.col()).toChar()}${pos.row() + 1}" to flag
            } }

            val io = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer.renderBoard(board))
                .map { producer.attachButtons(it.first(), config.language.container, focusedFields) }
                .map { it.launch(); Order.Unit }

            io to this.asCommandReport("succeed")
        }
        DebugType.STATUS -> {
            IO { Order.Unit } to this.asCommandReport("succeed")
        }
    } }

}