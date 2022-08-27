@file:Suppress("unused")

package core.interact.commands

import core.BotContext
import core.assets.Guild
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
import core.session.entities.RequestSession
import jrenju.BoardIO
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.Deferred
import utils.assets.LinuxTime
import utils.lang.and
import utils.lang.toInputStream
import utils.structs.IO
import utils.structs.Option
import utils.structs.flatMap
import utils.structs.map

enum class DebugType {
    ANALYSIS, SELF_REQUEST, VCF, INJECT, STATUS
}

class DebugCommand(
    override val name: String,
    private val debugType: DebugType,
    private val payload: String?,
) : Command {

    @Suppress("DuplicatedCode")
    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching { when (debugType) {
        DebugType.ANALYSIS -> {
            SessionManager.retrieveGameSession(bot.sessions, guild, user.id)?.let { session ->
                producer.produceSessionArchive(publisher, session, session.gameResult)
                    .flatMap {
                        it.addFile(session.board.toBoardIO().debugText().toInputStream(), "analysis-report-${System.currentTimeMillis()}.txt")
                            .launch()

                    }
                    .map { emptyList<Order>() } and this.asCommandReport("succeed", user)
            } ?: (IO { emptyList<Order>() } and this.asCommandReport("failed", user))
        }
        DebugType.SELF_REQUEST -> {
            if (SessionManager.retrieveGameSession(bot.sessions, guild, user.id) != null ||
                SessionManager.retrieveRequestSession(bot.sessions, guild, user.id) != null)
                IO { emptyList<Order>() } and this.asCommandReport("failed", user)
            else {
                val requestSession = RequestSession(
                    user, user,
                    SessionManager.generateMessageBufferKey(user),
                    LinuxTime.withExpireOffset(bot.config.gameExpireOffset)
                )

                SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                val io = producer.produceRequest(publisher, config.language.container, user, user)
                    .flatMap { it.launch() }
                    .map { emptyList<Order>()  }

                io and this.asCommandReport("succeed", user)
            }
        }
        DebugType.INJECT -> {
            val board = BoardIO.fromBoardText(this.payload, Renju.BOARD_CENTER_POS().idx()).get()
            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                solution = Option.Empty,
                ownerHasBlack = board.isNextColorBlack,
                board = board,
                history = List(board.moves()) { null },
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.gameExpireOffset,
                recording = false,
                expireDate = LinuxTime.withExpireOffset(bot.config.gameExpireOffset)
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = producer.produceNextMovePVE(publisher, config.language.container, user, session.board.latestPos().get())
                .flatMap { buildBoardSequence(bot, guild, config, producer, publisher, session) }
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", user)
        }
        DebugType.STATUS -> {
            IO { emptyList<Order>() } and this.asCommandReport("succeed", user)
        }
        DebugType.VCF -> {
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
                """.trimIndent()
                .let { BoardIO.fromBoardText(it, Pos.fromCartesian("m3").get().idx()).get() }

            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                solution = Option.Empty,
                ownerHasBlack = false,
                board = vcfCase,
                history = List(vcfCase.moves()) { null },
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.gameExpireOffset,
                recording = false,
                expireDate = LinuxTime.withExpireOffset(bot.config.gameExpireOffset)
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = producer.produceNextMovePVE(publisher, config.language.container, user, session.board.latestPos().get())
                .flatMap { buildBoardSequence(bot, guild, config, producer, publisher, session) }
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", user)
        }
    } }

}
