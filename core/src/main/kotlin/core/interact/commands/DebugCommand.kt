@file:Suppress("unused")

package core.interact.commands

import arrow.core.None
import arrow.core.raise.effect
import arrow.core.toOption
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.Notation
import core.assets.User
import core.database.entities.GameRecordId
import core.database.entities.asGameSession
import core.database.repositories.GameRecordRepository
import core.inference.AiLevel
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.Rule
import core.session.SessionManager
import core.session.entities.*
import renju.BoardIO
import renju.notation.Pos
import renju.notation.Renju
import utils.assets.LinuxTime
import utils.lang.toInputStream
import utils.lang.tuple

enum class DebugType {
    ANALYSIS, SELF_REQUEST, VCF, INJECT, STATUS, SESSIONS, GIF
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
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching { when (debugType) {
        DebugType.ANALYSIS -> {
            SessionManager.retrieveGameSession(bot.sessions, guild, user.id)?.let { session ->
                effect {
                    service.buildSessionArchive(publishers.plain, session, session.gameResult, false)
                        .addFile(
                            Notation.BoardIOInstance.buildBoardDebugString(session.board).toInputStream(),
                            "analysis-report-${System.currentTimeMillis()}.txt"
                        )
                        .launch()()

                    emptyOrders
                }.let { tuple(it, this.writeCommandReport("succeed", guild, user)) }
            } ?: (tuple(effect { emptyOrders }, this.writeCommandReport("failed", guild, user)))
        }
        DebugType.SELF_REQUEST -> {
            if (SessionManager.retrieveGameSession(bot.sessions, guild, user.id) != null ||
                        SessionManager.retrieveRequestSession(bot.sessions, guild, user.id) != null)
                tuple(effect { emptyOrders }, this.writeCommandReport("failed", guild, user))
            else {
                val requestSession =
                    RequestSession(
                        user, user,
                        MessageBufferKey.issue(),
                        Rule.RENJU,
                        LinuxTime.nowWithOffset(bot.config.gameExpireOffset),
                    )

                    SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                    val io = effect {
                        service.buildRequest(publishers.plain, config.language.container, user, user, Rule.RENJU)
                            .launch()()

                        emptyOrders
                    }

                    tuple(io, this.writeCommandReport("succeed", guild, user))
            }
        }
        DebugType.INJECT -> {
            val board = BoardIO.fromBoardText(this.payload, Renju.BOARD_CENTER_POS().idx()).get()

            val session = AiGameSession(
                owner = user,
                aiLevel = AiLevel.AMOEBA,
                solution = None,
                ownerHasBlack = board.isNextColorBlack,
                board = board,
                history = List(board.moves()) { null },
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireOffset),
                ruleKind = Rule.RENJU,
                recording = false,
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = effect {
                service.buildNextMovePVE(publishers.plain, config.language.container, user, session.board.lastPos().get())
                    .launch()()

                buildBoardProcedure(bot, guild, config, service, publishers.plain, session)()

                emptyOrders
            }

            tuple(io, this.writeCommandReport("succeed", guild, user))
        }
        DebugType.STATUS -> {
            val message = """
                ${this.payload}
                games = ${bot.sessions.sessions.map { (_, session) -> session.gameSessions.size }.sum()}
                requests = ${bot.sessions.sessions.map { (_, session) -> session.requestSessions.size }.sum()}
                navigates = ${bot.sessions.navigates.size}
            """.trimIndent()

            val io = effect {
                service.buildDebugMessage(publishers.plain, message)
                    .launch()()

                emptyOrders
            }

            tuple(io, this.writeCommandReport("succeed", guild, user))
        }
        DebugType.SESSIONS -> {
            val sessionMessage = bot.sessions.sessions
                .flatMap { (_, session) -> session.gameSessions.values }
                .sortedBy { it.expireService.createDate.timestamp }
                .map { it.toString() }
                .let { sessions -> when {
                    sessions.isEmpty() -> "empty"
                    else -> sessions.reduce { acc, s -> "$acc\n$s" }
                } }

            val io = effect {
                service.buildDebugMessage(publishers.plain, "report here")
                    .addFile(sessionMessage.byteInputStream(), "sessions.txt")
                    .launch()()

                emptyOrders
            }

            tuple(io, this.writeCommandReport("succeed", guild, user))
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
                solution = None,
                ownerHasBlack = false,
                board = vcfCase,
                history = List(vcfCase.moves()) { null },
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireOffset),
                ruleKind = Rule.TARAGUCHI_10,
                recording = false,
            )

            SessionManager.putGameSession(bot.sessions, guild, session)

            val io = effect {
                service.buildNextMovePVE(publishers.plain, config.language.container, user, session.board.lastPos().get())
                    .launch()()

                buildBoardProcedure(bot, guild, config, service, publishers.plain, session)()

                emptyOrders
            }

            tuple(io, this.writeCommandReport("succeed", guild, user))
        }
        DebugType.GIF -> {
            val gameRecord = payload
                ?.split(" ")
                ?.getOrNull(2)
                ?.toLongOrNull()
                ?.let { GameRecordId(it) }
                .toOption()
                .fold(
                    ifSome = { GameRecordRepository.retrieveGameRecordByRecordId(bot.sessions.dbConnection, it) },
                    ifEmpty = { GameRecordRepository.retrieveLastGameRecordByUserUid(bot.sessions.dbConnection, user.id) }
                )
                .getOrNull()!!

            val session = gameRecord.asGameSession(bot.dbConnection, user)

            val io = effect {
                service.buildSessionArchive(publishers.plain, session, session.gameResult, true)
                    .launch()()

                emptyOrders
            }

            tuple(io, this.writeCommandReport("succeed", guild, user))
        }
    } }

}
