package core.session

import core.assets.User
import core.assets.aiUser
import core.inference.AiLevel
import core.inference.KvineClient
import core.interact.message.graphics.*
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import core.session.entities.nextWith
import jrenju.Board
import jrenju.`EmptyBoard$`
import jrenju.notation.Color
import jrenju.notation.Pos
import jrenju.notation.Renju
import jrenju.protocol.SolutionNode
import scala.Enumeration
import utils.assets.LinuxTime
import utils.structs.Identifiable
import utils.structs.Option
import utils.structs.flatMap
import utils.structs.fold
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

enum class SweepPolicy(override val id: Short) : Identifiable {
    RELAY(0), LEAVE(1)
}

enum class ArchivePolicy(override val id: Short) : Identifiable {
    WITH_PROFILE(0), BY_ANONYMOUS(1), PRIVACY(2)
}

enum class RejectReason(override val id: Short) : Identifiable {
    EXIST(0), FORBIDDEN(1)
}

sealed interface GameResult {

    val cause: Cause

    val message: String

    val winColorId: Short?

    enum class Cause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(0), RESIGN(1), TIMEOUT(2), DRAW(3)
    }

    data class Win(override val cause: Cause, val winColor: Enumeration.Value, val winner: User, val looser: User) : GameResult {

        override val message get() = "$winner wins over $looser by $cause"

        override val winColorId = this.winColor.id().toShort()

    }

    object Full : GameResult {

        override val cause = Cause.DRAW

        override val message get() = "tie caused by full"

        override val winColorId = null

    }

    companion object {

        fun build(cause: Cause, blackUser: User?, whiteUser: User?, winColor: Enumeration.Value?): GameResult? =
            when (cause) {
                Cause.FIVE_IN_A_ROW, Cause.RESIGN, Cause.TIMEOUT -> when (winColor) {
                    Color.BLACK() -> Win(cause, Color.BLACK(), blackUser ?: aiUser, whiteUser ?: aiUser)
                    Color.WHITE() -> Win(cause, Color.WHITE(), whiteUser ?: aiUser, blackUser ?: aiUser)
                    else -> null
                }
                Cause.DRAW -> Full
            }

    }

}

object GameManager {

    fun generatePvpSession(expireOffset: Long, owner: User, opponent: User): GameSession =
        PvpGameSession(
            owner = owner,
            opponent = opponent,
            ownerHasBlack = Random(System.currentTimeMillis()).nextBoolean(),
            board = `EmptyBoard$`.`MODULE$`,
            history = emptyList(),
            messageBufferKey = SessionManager.generateMessageBufferKey(owner),
            recording = true,
            expireOffset = expireOffset,
            expireDate = LinuxTime.withExpireOffset(expireOffset),
        )

    fun generateAiSession(expireOffset: Long, owner: User, aiLevel: AiLevel): GameSession {
        val ownerHasBlack = Random(System.currentTimeMillis()).nextBoolean()

        val board = if (ownerHasBlack) `EmptyBoard$`.`MODULE$` else Board.newBoard()

        return AiGameSession(
            owner = owner,
            aiLevel = aiLevel,
            solution = Option.Empty,
            ownerHasBlack = ownerHasBlack,
            board = board,
            history = if (ownerHasBlack) emptyList() else listOf(Renju.BOARD_CENTER_POS()),
            messageBufferKey = SessionManager.generateMessageBufferKey(owner),
            recording = true,
            expireOffset = expireOffset,
            expireDate = LinuxTime.withExpireOffset(expireOffset),
        )
    }

    fun validateMove(session: GameSession, pos: Pos): Option<RejectReason> =
        session.board.validateMove(pos.idx()).fold(
            { Option.Empty },
            {
                when (it) {
                    jrenju.notation.RejectReason.EXIST() -> Option(RejectReason.EXIST)
                    jrenju.notation.RejectReason.FORBIDDEN() -> Option(RejectReason.FORBIDDEN)
                    else -> throw IllegalStateException()
                }
            }
        )

    fun makeMove(session: GameSession, pos: Pos): GameSession {
        val thenBoard = session.board.makeMove(pos)

        if (session.board.moves() + 1 >= Renju.BOARD_SIZE())
            return session.nextWith(
                thenBoard, pos,
                Option(GameResult.Full),
                session.messageBufferKey
            )

        return thenBoard.winner().fold(
            { session.nextWith(thenBoard, pos, Option.Empty) },
            {
                session.nextWith(
                    thenBoard, pos,
                    Option(GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, thenBoard.color(), session.player, session.nextPlayer)),
                    session.messageBufferKey
                )
            }
        )
    }

    fun makeAiMove(kvineClient: KvineClient, session: AiGameSession, latestMove: Pos): AiGameSession {
        val (aiMove, solutionNode) = session.solution
            .flatMap { solutionNode ->
                solutionNode.child().get(latestMove.idx()).fold(
                    { Option.Empty },
                    { Option(it) }
                )
            }
            .fold(
                onDefined = {
                    when (it) {
                        is SolutionNode -> it.idx() to Option(it)
                        else -> it.idx() to Option.Empty
                    }
                },
                onEmpty = {
                    when (val solution = session.aiLevel.solver(kvineClient, session.board, Pos.fromIdx(session.board.latestMove()))) {
                        is SolutionNode -> solution.idx() to Option(solution)
                        else -> solution.idx() to Option.Empty
                    }
                },
            )

        val thenBoard = session.board.makeMove(aiMove)

        return if (thenBoard.winner().isDefined) {
            val gameResult = GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, thenBoard.color(), aiUser, session.owner)

            session.copy(
                board = thenBoard,
                gameResult = Option(gameResult),
                history = session.history + Pos.fromIdx(aiMove),
            )
        } else if (thenBoard.moves() == Renju.BOARD_SIZE()) {
            val gameResult = GameResult.Full

            session.copy(
                board = thenBoard,
                gameResult = Option(gameResult),
                history = session.history + Pos.fromIdx(aiMove)
            )
        } else session.copy(
            board = thenBoard,
            history = session.history + Pos.fromIdx(aiMove),
            solution = solutionNode
        )
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User?) =
        when (session) {
            is AiGameSession -> {
                val result = GameResult.Win(cause, session.board.color(), aiUser, session.owner)

                session.copy(gameResult = Option(result)) to result
            }
            is PvpGameSession -> {
                val (winner, looser) = if (user?.id == session.owner.id)
                    session.opponent to session.owner
                else
                    session.owner to session.opponent

                val result = GameResult.Win(cause, session.board.color(), winner, looser)

                session.copy(gameResult = Option(result)) to result
            }
        }

}
