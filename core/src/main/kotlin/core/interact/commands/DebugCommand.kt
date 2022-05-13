@file:Suppress("unused")

package core.interact.commands

import core.BotContext
import core.assets.User
import core.assets.toBoardIO
import core.inference.AiLevel
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import core.session.entities.RequestSession
import jrenju.BoardIO
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.Deferred
import utils.assets.LinuxTime
import utils.lang.toInputStream
import utils.structs.IO
import utils.structs.Option

enum class DebugType {
    BOARD_DEMO, BOARD_ANALYSIS, SELF_REQUEST, VCF_SESSION, INJECT_BOARD, STATUS
}

class DebugCommand(override val command: String, private val debugType: DebugType, private val payload: String?) : Command {

    @Suppress("DuplicatedCode")
    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching { when (debugType) {
        DebugType.BOARD_ANALYSIS -> {
            SessionManager.retrieveGameSession(bot.sessionRepository, config.id, user.id)?.let { session ->
                producer.produceSessionArchive(publisher, session)
                    .map { it.addFile(session.board.toBoardIO().debugText().toInputStream(), "analysis-report-${System.currentTimeMillis()}.txt") }
                    .map { it.launch(); Order.Unit } to this.asCommandReport("succeed", user)
            } ?: (IO { Order.Unit } to this.asCommandReport("failed", user))
        }
        DebugType.SELF_REQUEST -> {
            if (SessionManager.retrieveGameSession(bot.sessionRepository, config.id, user.id) != null ||
                SessionManager.retrieveRequestSession(bot.sessionRepository, config.id, user.id) != null)
                IO { Order.Unit } to this.asCommandReport("failed", user)
            else {
                val requestSession = RequestSession(
                    user, user,
                    SessionManager.generateMessageBufferKey(user),
                    LinuxTime(System.currentTimeMillis() + bot.config.expireOffset),
                )

                SessionManager.putRequestSession(bot.sessionRepository, config.id, requestSession)

                val io = producer.produceRequest(publisher, config.language.container, user, user).map { it.launch(); Order.Unit }

                io to this.asCommandReport("succeed", user)
            }
        }
        DebugType.INJECT_BOARD -> {
            val board = BoardIO.fromBoardText(this.payload, Renju.BOARD_CENTER_POS().idx()).get()
            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                solution = Option.Empty,
                ownerHasBlack = board.isNextColorBlack,
                board = BoardIO.fromBoardText(payload, Pos.fromCartesian(this.payload!!.take(3).trim(' ')).get().idx()).get(),
                history = if (board.isNextColorBlack) emptyList() else listOf(Renju.BOARD_CENTER_POS()),
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.expireOffset,
                expireDate = LinuxTime.withExpireOffset(bot.config.expireOffset)
            )
            SessionManager.putGameSession(bot.sessionRepository, config.id, session)

            val io = producer.produceNextMovePVE(publisher, config.language.container, user, session.board.latestPos().get())
                .attachBoardSequence(bot, config, producer, publisher, session)
                .map { Order.Unit }

            io to this.asCommandReport("succeed", user)
        }
        DebugType.STATUS -> {
            IO { Order.Unit } to this.asCommandReport("succeed", user)
        }
        DebugType.BOARD_DEMO -> {
            val complexCase = """
                   A B C D E F G H I J K L M N O
                15 . . . . . . . . . . . . . . . 15
                14 . . . . . . . . . . . . . . . 14
                13 . . . . . . . . . . . . . . . 13
                12 . . . . . . . . . . . . . . . 12
                11 . . . O . . O . . . . . . . . 11
                10 . . . . X . . X . O . . . . . 10
                 9 . . . . O X O X X . . . . . . 9
                 8 . . . . . . X X . . . . . . . 8
                 7 . . . . . . O O X X . . . . . 7
                 6 . . . . . X . . . . . . . . . 6
                 5 . . . . O X . . . . . . . . . 5
                 4 . . . . . O . . . . . . . . . 4
                 3 . . . . . . . . . . . . . . . 3
                 2 . . . . . . . . . . . . . . . 2
                 1 . . . . . . . . . . . . . . . 1
                   A B C D E F G H I J K L M N O
            """.trimMargin()

            val board = BoardIO.fromBoardText(complexCase, Pos.fromCartesian("i9").get().idx()).get()

            val session = PvpGameSession(user, user, true, board, Option.Empty, emptyList(),  "debug", 0, LinuxTime(0))

            val io = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session)
                .map { producer.attachFocusButtons(it, session, Pos.fromCartesian("j10").get()) }
                .map { it.launch(); Order.Unit }

            io to this.asCommandReport("succeed", user)
        }
        DebugType.VCF_SESSION -> {
            val vcfCase = """
                       A B C D E F G H I J K L M N O
                    15 O . . . X . . . . . . . X . X 15
                    14 X . . . . O . . . O . . O . X 14
                    13 . . . . . . . O . . . . . O . 13
                    12 O . . . . . . . . . . X . . X 12
                    11 X . . . . . . . . . . . O . . 11
                    10 O . O . . . . . . . . . . . . 10
                     9 O O X O . . . . X . . . O . . 9
                     8 O . O O . . . X . O . . . . . 8
                     7 . X . . . . . . . O . . X . . 7
                     6 . . . . . . . . O . . . . . X 6
                     5 X . . . . . . . . . . . X . X 5
                     4 . . . . . . . . . . . . . X O 4
                     3 X . . . . . . . . . . . X X . 3
                     2 . . . . . . . X . . . . . . O 2
                     1 X O O O . X . . X . X . . . . 1
                       A B C D E F G H I J K L M N O
                """.trimMargin()
            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                solution = Option.Empty,
                ownerHasBlack = false,
                board = BoardIO.fromBoardText(vcfCase, Pos.fromCartesian("m3").get().idx()).get(),
                history = emptyList(),
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.expireOffset,
                expireDate = LinuxTime.withExpireOffset(bot.config.expireOffset)
            )
            SessionManager.putGameSession(bot.sessionRepository, config.id, session)

            val io = producer.produceNextMovePVE(publisher, config.language.container, user, session.board.latestPos().get())
                .attachNextMoveSequence(bot, config, producer, publisher, session, session)

            io to this.asCommandReport("succeed", user)
        }
    } }

}
