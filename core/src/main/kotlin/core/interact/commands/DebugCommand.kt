@file:Suppress("unused")

package core.interact.commands

import core.BotContext
import core.assets.User
import core.assets.toBoardIO
import core.interact.Order
import core.interact.message.MessageModifier
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import core.session.entities.RequestSession
import jrenju.BoardIO
import jrenju.notation.Pos
import utils.assets.LinuxTime
import utils.lang.toInputStream
import utils.structs.IO
import utils.structs.Option

enum class DebugType {
    BOARD_DEMO, BOARD_ANALYSIS, SELF_REQUEST, STATUS
}

class DebugCommand(override val command: String, private val debugType: DebugType, private val payload: String?) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        modifier: MessageModifier<A, B>,
    ) = runCatching { when (debugType) {
        DebugType.BOARD_DEMO -> {
            val complexCase =
                "   A B C D E F G H I J K L M N O   \n" +
                "15 . . . . . . . . . . . . . . . 15\n" +
                "14 . . . . . . . . . . . . . . . 14\n" +
                "13 . . . . . . . . . O . . . . . 13\n" +
                "12 . . . . . . . O X X . . . . . 12\n" +
                "11 . . . . . . . . X . O O . . . 11\n" +
                "10 . . . . O X O O O X . . . . . 10\n" +
                " 9 . . . X X O O X X . . . . . . 9 \n" +
                " 8 . . . O X X X O X O . . . . . 8 \n" +
                " 7 . . . X . O O O X X O . . . . 7 \n" +
                " 6 . . O O X X X . O O X . . . . 6 \n" +
                " 5 . X O O O X . X O X . O . . . 5 \n" +
                " 4 . . O X X X O X O X . . . . . 4 \n" +
                " 3 . . . . . O . . O . . . . . . 3 \n" +
                " 2 . . . . . . . . X . . . . . . 2 \n" +
                " 1 . . . . . . . . . . . . . . . 1 \n" +
                "   A B C D E F G H I J K L M N O   "

            val board = BoardIO.fromBoardText(complexCase, Pos(5, 2).idx(), scala.Option.empty()).get()
                .calculateGlobalL2Board()
                .calculateL3Board()
                .calculateDeepL3Board()

            val session = PvpGameSession(user, user, true, board, Option.Empty, emptyList(), "debug", LinuxTime())

            val io = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session)
                .map { producer.attachFocusButtons(it, config.language.container, config.focusPolicy, session) }
                .map { it.launch(); Order.Unit }

            io to this.asCommandReport("succeed", user)
        }
        DebugType.BOARD_ANALYSIS -> {
            SessionManager.retrieveGameSession(context.sessionRepository, config.id, user.id)?.let { session ->
                producer.producePublicBoard(publisher, session)
                    .map { action ->
                        action
                            .addFile(session.board.toBoardIO().debugText().toInputStream(), "analysis-${System.currentTimeMillis()}.txt")
                            .launch()
                        Order.Unit
                    } to this.asCommandReport("succeed", user)
            }
                ?: (IO { Order.Unit } to this.asCommandReport("failed", user))
        }
        DebugType.SELF_REQUEST -> {
            if (SessionManager.retrieveGameSession(context.sessionRepository, config.id, user.id) != null ||
                    SessionManager.retrieveRequestSession(context.sessionRepository, config.id, user.id) != null)
                IO { Order.Unit } to this.asCommandReport("failed", user)
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
