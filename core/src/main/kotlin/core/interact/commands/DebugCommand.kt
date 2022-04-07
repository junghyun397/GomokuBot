@file:Suppress("unused")

package core.interact.commands

import core.BotContext
import core.assets.User
import core.inference.InferenceManager
import core.interact.Order
import core.interact.message.ButtonFlag
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import jrenju.AttackPoints
import jrenju.BoardTransform
import jrenju.L2Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.rule.Renju
import utils.assets.LinuxTime
import utils.structs.IO
import utils.structs.Option

enum class DebugType {
    BOARD_DEMO, SELF_REQUEST, STATUS
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
            val complexCase =
                "   A B C D E F G H I J K L M N O   \n" +
                "15 . . . . . . . . . . . . . . . 15\n" +
                "14 . . . . . . . . . . . . . . . 14\n" +
                "13 . . . . . . . . . O . . . . . 13\n" +
                "12 . . . . . . . O X X . . . . . 12\n" +
                "11 . . . . . . . . . . O O . . . 11\n" +
                "10 . . . . O X O O O X . . . . . 10\n" +
                " 9 . . . X X O O X X . . . . . .  9\n" +
                " 8 . . . O X X X O X O . . . . .  8\n" +
                " 7 . . . X . O O O X X O . . . .  7\n" +
                " 6 . . O O X X X . O O X . . . .  6\n" +
                " 5 . X O O O X . X O X . O . . .  5\n" +
                " 4 . . O X X X O X O X . . . . .  4\n" +
                " 3 . . . . . O . . O . . . . . .  3\n" +
                " 2 . . . . . . . . X . . . . . .  2\n" +
                " 1 . . . . . . . . . . . . . . .  1\n" +
                "   A B C D E F G H I J K L M N O   "

            val board = BoardTransform.fromBoardText(complexCase, Pos(2, 5).idx(), scala.Option.empty()).get()
                .let {
                    L2Board(
                        it.boardField(),
                        Array(Renju.BOARD_LENGTH()) { AttackPoints(0, 0, 0, 0, 0, 0, 0, 0) },
                        it.moves(),
                        it.latestMove(),
                        it.opening(),
                        false
                    )
                }
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
                "${(97 + pos.row()).toChar()}${pos.col() + 1}" to flag
            } }

            val session = GameSession(user, Option.Empty, true, board, emptyList(), emptyList(), LinuxTime())

            val io = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session)
                .map { producer.attachButtons(it, config.language.container, focusedFields) }
                .map { it.launch(); Order.Unit }

            io to this.asCommandReport("succeed", user)
        }
        DebugType.SELF_REQUEST -> {
            if (SessionManager.retrieveGameSession(context.sessionRepository, config.id, user.id) != null ||
                    SessionManager.retrieveRequestSession(context.sessionRepository, config.id, user.id) != null)
                IO { Order.Unit } to this.asCommandReport("failure", user)
            else {
                val requestSession = RequestSession(user, user, LinuxTime())
                SessionManager.putRequestSession(context.sessionRepository, config.id, requestSession)

                val io = producer.produceRequest(publisher, config.language.container, user, user).map { it.launch(); Order.Unit }

                io to this.asCommandReport("succeed", user)
            }
        }
        DebugType.STATUS -> {
            IO { Order.Unit } to this.asCommandReport("succeed", user)
        }
    } }

}