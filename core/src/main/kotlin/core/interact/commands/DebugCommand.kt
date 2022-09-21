@file:Suppress("unused")

package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.assets.toBoardIO
import core.inference.AiLevel
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.Token
import core.session.entities.AiGameSession
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import jrenju.BoardIO
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.assets.LinuxTime
import utils.lang.and
import utils.lang.toInputStream
import utils.structs.IO
import utils.structs.Option
import utils.structs.flatMap
import utils.structs.map

enum class DebugType {
    ANALYSIS, SELF_REQUEST, VCF, INJECT, STATUS, SESSIONS
}

class DebugCommand(
    private val debugType: DebugType,
    private val payload: String?,
) : Command {

    override val name = "debug"

    override val responseFlag = ResponseFlag.Defer

    @Suppress("DuplicatedCode")
    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching { when (debugType) {
        DebugType.ANALYSIS -> {
            SessionManager.retrieveGameSession(bot.sessions, guild, user.id)?.let { session ->
                producer.produceSessionArchive(publishers.plain, session, session.gameResult)
                    .addFile(session.board.toBoardIO().debugString().toInputStream(), "analysis-report-${System.currentTimeMillis()}.txt")
                    .launch()
                    .map { emptyList<Order>() } and this.asCommandReport("succeed", guild, user)
            } ?: (IO { emptyList<Order>() } and this.asCommandReport("failed", guild, user))
        }
        DebugType.SELF_REQUEST -> {
            when {
                SessionManager.retrieveGameSession(bot.sessions, guild, user.id) != null ||
                        SessionManager.retrieveRequestSession(bot.sessions, guild, user.id) != null ->
                    IO { emptyList<Order>() } and this.asCommandReport("failed", guild, user)
                else -> {
                    val requestSession = RequestSession(
                        user, user,
                        SessionManager.generateMessageBufferKey(user),
                        LinuxTime.nowWithOffset(bot.config.gameExpireOffset)
                    )

                    SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                    val io = producer.produceRequest(publishers.plain, config.language.container, user, user)
                        .launch()
                        .map { emptyList<Order>()  }

                    io and this.asCommandReport("succeed", guild, user)
                }
            }
        }
        DebugType.INJECT -> {
            val board = BoardIO.fromBoardText(this.payload, Renju.BOARD_CENTER_POS().idx()).get()

            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                kvineToken = Token(""),
                solution = Option.Empty,
                ownerHasBlack = board.isNextColorBlack,
                board = board,
                history = List(board.moves()) { null },
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.gameExpireOffset,
                recording = false,
                expireDate = LinuxTime.nowWithOffset(bot.config.gameExpireOffset)
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = producer.produceNextMovePVE(publishers.plain, config.language.container, user, session.board.latestPos().get())
                .launch()
                .flatMap { buildBoardProcedure(bot, guild, config, producer, publishers.plain, session) }
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", guild, user)
        }
        DebugType.STATUS -> {
            val message = """
                games = ${bot.sessions.sessions.map { (_, session) -> session.gameSessions.size }.sum()}
                requests = ${bot.sessions.sessions.map { (_, session) -> session.requestSessions.size }.sum()}
                navigates = ${bot.sessions.navigates.size}
            """.trimIndent()

            val io = producer.produceDebugMessage(publishers.plain, message)
                .launch()
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", guild, user)
        }
        DebugType.SESSIONS -> {
            val sessionMessage = bot.sessions.sessions
                .flatMap { (_, session) -> session.gameSessions.values }
                .map { it.toString() }
                .let { sessions -> when {
                    sessions.isEmpty() -> ""
                    else -> sessions.reduce { acc, s -> "$acc\n$s" }
                } }

            val io = producer.produceDebugMessage(publishers.plain, "report here")
                .addFile(sessionMessage.byteInputStream(), "sessions.txt")
                .launch()
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", guild, user)
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
                kvineToken = Token(""),
                solution = Option.Empty,
                ownerHasBlack = false,
                board = vcfCase,
                history = List(vcfCase.moves()) { null },
                messageBufferKey = SessionManager.generateMessageBufferKey(user),
                expireOffset = bot.config.gameExpireOffset,
                recording = false,
                expireDate = LinuxTime.nowWithOffset(bot.config.gameExpireOffset)
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = producer.produceNextMovePVE(publishers.plain, config.language.container, user, session.board.latestPos().get())
                .launch()
                .flatMap { buildBoardProcedure(bot, guild, config, producer, publishers.plain, session) }
                .map { emptyList<Order>() }

            io and this.asCommandReport("succeed", guild, user)
        }
    } }

}
