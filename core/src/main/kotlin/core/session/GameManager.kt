package core.session

import core.BotContext
import core.assets.*
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.inference.AiLevel
import core.inference.FocusSolver
import core.inference.KvineClient
import core.interact.message.graphics.*
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import renju.`EmptyScalaBoard$`
import renju.ScalaBoard
import renju.notation.*
import renju.protocol.SolutionNode
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.*
import kotlin.random.Random

enum class BoardStyle(override val id: Short, val renderer: BoardRenderer, val sample: BoardRendererSample) : Identifiable {
    IMAGE(0, ImageBoardRenderer, ImageBoardRenderer),
    TEXT(1, TextBoardRenderer(), TextBoardRenderer),
    SOLID_TEXT(2, SolidTextBoardRenderer(), SolidTextBoardRenderer),
    UNICODE(3, UnicodeBoardRenderer, UnicodeBoardRenderer)
}

enum class FocusPolicy(override val id: Short) : Identifiable {
    INTELLIGENCE(0), FALLOWING(1)
}

enum class HintPolicy(override val id: Short) : Identifiable {
    OFF(0), FIVE(1)
}

enum class SweepPolicy(override val id: Short) : Identifiable {
    RELAY(0), LEAVE(1), EDIT(2)
}

enum class ArchivePolicy(override val id: Short) : Identifiable {
    WITH_PROFILE(0), BY_ANONYMOUS(1), PRIVACY(2)
}

@JvmInline value class Token(val token: String)

sealed interface GameResult {

    val cause: Cause

    val message: String

    val winColorId: Short?

    enum class Cause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(0), RESIGN(1), TIMEOUT(2), DRAW(3)
    }

    data class Win(override val cause: Cause, val winColor: Color, val winner: User, val looser: User) : GameResult {

        override val message get() = "$winner wins over $looser by $cause"

        override val winColorId = this.winColor.flag().toShort()

    }

    object Full : GameResult {

        override val cause = Cause.DRAW

        override val message get() = "tie caused by full"

        override val winColorId = null

    }

    companion object {

        fun build(gameResult: Result, cause: Cause, blackUser: User?, whiteUser: User?): GameResult? =
            when (cause) {
                Cause.FIVE_IN_A_ROW, Cause.RESIGN, Cause.TIMEOUT -> when (gameResult.flag()) {
                    Notation.FlagInstance.BLACK() ->
                        Win(cause, Notation.Color.Black, blackUser ?: aiUser, whiteUser ?: aiUser)
                    Notation.FlagInstance.WHITE() ->
                        Win(cause, Notation.Color.White, whiteUser ?: aiUser, blackUser ?: aiUser)
                    else -> null
                }
                Cause.DRAW -> Full
            }

    }

}

object GameManager {

    fun generatePvpSession(bot: BotContext, owner: User, opponent: User): GameSession =
        PvpGameSession(
            owner = owner,
            opponent = opponent,
            ownerHasBlack = Random(System.currentTimeMillis()).nextBoolean(),
            board = `EmptyScalaBoard$`.`MODULE$`,
            history = emptyList(),
            messageBufferKey = SessionManager.generateMessageBufferKey(owner),
            recording = true,
            expireOffset = bot.config.gameExpireOffset,
            expireDate = LinuxTime.nowWithOffset(bot.config.gameExpireOffset),
        )

    suspend fun generateAiSession(bot: BotContext, owner: User, aiLevel: AiLevel): GameSession {
        val ownerHasBlack = java.util.Random().nextBoolean()

        val board = if (ownerHasBlack) `EmptyScalaBoard$`.`MODULE$` else ScalaBoard.newBoard()

        val history = if (ownerHasBlack) emptyList() else listOf(Renju.BOARD_CENTER_POS())

        val aiColor = if (ownerHasBlack) Notation.Color.White else Notation.Color.Black

        val token = when (aiLevel) {
            AiLevel.AMOEBA -> Token("AMOEBA")
            else -> bot.kvineClient.begins(aiLevel.aiPreset, aiColor, board)
        }

        return AiGameSession(
            owner = owner,
            aiLevel = aiLevel,
            kvineToken = token,
            solution = Option.Empty,
            ownerHasBlack = ownerHasBlack,
            board = board,
            history = history,
            messageBufferKey = SessionManager.generateMessageBufferKey(owner),
            recording = true,
            expireOffset = bot.config.gameExpireOffset,
            expireDate = LinuxTime.nowWithOffset(bot.config.gameExpireOffset),
        )
    }

    fun validateMove(session: GameSession, pos: Pos): Option<InvalidKind> =
        session.board.validateMove(pos.idx()).toOption()

    fun makeMove(session: GameSession, pos: Pos): GameSession {
        val thenBoard = session.board.makeMove(pos)

        return thenBoard.winner().fold(
            { session.next(thenBoard, pos, Option.Empty, SessionManager.generateMessageBufferKey(session.owner)) },
            { result ->
                val gameResult = when (result) {
                    is Result.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), session.player, session.nextPlayer)
                    else -> GameResult.Full
                }

                session.next(thenBoard, pos, Option(gameResult), session.messageBufferKey)
            }
        )
    }

    suspend fun makeAiMove(kvineClient: KvineClient, session: AiGameSession): AiGameSession {
        val (aiMove, solutionNode) = session.solution
            .flatMap { solutionNode ->
                solutionNode.child().get(session.board.lastMove()).fold(
                    { Option.Empty },
                    { Option(it) }
                )
            }
            .fold(
                onDefined = {
                    when (it) {
                        is SolutionNode -> it.idx() and Option(it)
                        else -> it.idx() and Option.Empty
                    }
                },
                onEmpty = {
                    val solution = when (session.aiLevel) {
                        AiLevel.AMOEBA -> FocusSolver.findSolution(session.board)
                        else -> kvineClient.update(session.kvineToken, session.board)
                    }

                    when (solution) {
                        is SolutionNode -> solution.idx() and Option(solution)
                        else -> solution.idx() and Option.Empty
                    }
                },
            )

        val thenBoard = session.board.makeMove(aiMove)

        return thenBoard.winner().fold(
            {
                session.copy(
                    board = thenBoard,
                    history = session.history + Pos.fromIdx(aiMove),
                    solution = solutionNode
                )
            },
            { result ->
                val gameResult = when (result) {
                    is Result.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), aiUser, session.owner)
                    else -> GameResult.Full
                }

                session.copy(
                    board = thenBoard,
                    gameResult = Option(gameResult),
                    history = session.history + Pos.fromIdx(aiMove),
                )
            }
        )
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User?): Pair<GameSession, GameResult.Win> {
        val winColor = when (session.board) {
            is `EmptyScalaBoard$` -> when (session.ownerHasBlack) {
                true -> Notation.Color.White
                else -> Notation.Color.Black
            }
            else -> session.board.color()
        }

        return when (session) {
            is AiGameSession -> {
                val result = GameResult.Win(cause, winColor, aiUser, session.owner)

                session.copy(gameResult = Option(result)) and result
            }
            is PvpGameSession -> {
                val (winner, looser) = if (user?.id == session.owner.id)
                    session.opponent and session.owner
                else
                    session.owner and session.opponent

                val result = GameResult.Win(cause, winColor, winner, looser)

                session.copy(gameResult = Option(result)) and result
            }
        }
    }

    suspend fun finishSession(bot: BotContext, guild: Guild, session: GameSession, result: GameResult) {
        SessionManager.removeGameSession(bot.sessions, guild, session.owner.id)

        session.extractGameRecord(guild.id).forEach { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }

        if (session is AiGameSession && session.aiLevel != AiLevel.AMOEBA) {
            bot.kvineClient.report(session.kvineToken, session.board, result)
        }
    }

}
