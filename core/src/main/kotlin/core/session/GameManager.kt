package core.session

import core.assets.User
import core.assets.aiUser
import core.inference.AiLevel
import core.inference.B3nzeneClient
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import core.session.entities.nextWith
import jrenju.Board
import jrenju.`EmptyBoard$`
import jrenju.notation.Pos
import jrenju.notation.Renju
import jrenju.protocol.SolutionNode
import scala.Enumeration
import utils.assets.LinuxTime
import utils.structs.Option
import kotlin.random.Random

enum class FocusPolicy {
    INTELLIGENCE, FALLOWING
}

enum class SweepPolicy {
    RELAY, LEAVE
}

enum class ArchivePolicy {
    WITH_PROFILE, BY_ANONYMOUS, PRIVACY
}

enum class RejectReason {
    EXIST, FORBIDDEN
}

sealed interface GameResult {

    enum class WinCause {
        FIVE_IN_A_ROW, RESIGN, TIMEOUT
    }

    class Win(val winCause: WinCause, val winColor: Enumeration.Value, val winner: User, val looser: User) : GameResult {
        override fun toString() = "$winner wins over $looser by $winCause"
    }

    object Full : GameResult {
        override fun toString() = "tie caused by full"
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
            expireOffset = expireOffset,
            expireDate = LinuxTime.withExpireOffset(expireOffset),
        )
    }

    fun validateMove(session: GameSession, pos: Pos): Option<RejectReason> =
        session.board.validateMove(pos.idx()).let {
            if (it.isDefined) when (it.get()) {
                jrenju.notation.RejectReason.EXIST() -> Option.Some(RejectReason.EXIST)
                jrenju.notation.RejectReason.FORBIDDEN() -> Option.Some(RejectReason.FORBIDDEN)
                else -> Option.Some(RejectReason.EXIST)
            }
            else Option.Empty
        }

    fun makeMove(session: GameSession, pos: Pos): GameSession {
        val thenBoard = session.board.makeMove(pos)

        if (session.board.moves() + 1 >= Renju.BOARD_SIZE())
            return session.nextWith(
                thenBoard, pos,
                Option.Some(GameResult.Full),
                session.messageBufferKey
            )

        return thenBoard.winner().fold(
            { session.nextWith(thenBoard, pos, Option.Empty) },
            {
                session.nextWith(
                    thenBoard, pos,
                    Option.Some(GameResult.Win(GameResult.WinCause.FIVE_IN_A_ROW, thenBoard.color(), session.player, session.nextPlayer)),
                    session.messageBufferKey
                )
            }
        )
    }

    fun makeAiMove(b3nzeneClient: B3nzeneClient, session: AiGameSession, latestMove: Pos): AiGameSession {
        val (aiMove, solutionNode) = session.solution
            .flatMap { solutionNode ->
                solutionNode.child().get(latestMove.idx()).fold(
                    { Option.Empty },
                    { Option.Some(it) }
                )
            }
            .fold(
                onDefined = {
                    when(it) {
                        is SolutionNode -> it.idx() to Option.Some(it)
                        else -> it.idx() to Option.Empty
                    }
                },
                onEmpty = {
                    when (val solution = session.aiLevel.solver(
                        b3nzeneClient, session.board, Pos.fromIdx(session.board.latestMove()))
                    ) {
                        is SolutionNode -> solution.idx() to Option.Some(solution)
                        else -> solution.idx() to Option.Empty
                    }
                },
            )

        val thenBoard = session.board.makeMove(aiMove)

        return if (thenBoard.winner().isDefined) {
            val gameResult = GameResult.Win(GameResult.WinCause.FIVE_IN_A_ROW, thenBoard.color(), aiUser, session.owner)
            session.copy(
                board = thenBoard,
                gameResult = Option.Some(gameResult),
                history = session.history + Pos.fromIdx(aiMove),
            )
        } else session.copy(
            board = thenBoard,
            history = session.history + Pos.fromIdx(aiMove),
            solution = solutionNode
        )
    }

    fun resignSession(session: GameSession, user: User?) =
        when (session) {
            is AiGameSession -> {
                val result = GameResult.Win(GameResult.WinCause.RESIGN, session.board.color(), aiUser, session.owner)

                session.copy(gameResult = Option.Some(result)) to result
            }
            is PvpGameSession -> {
                val (winner, looser) = if (user?.id == session.owner.id)
                    session.opponent to session.owner
                else
                    session.owner to session.opponent

                val result = GameResult.Win(GameResult.WinCause.RESIGN, session.board.color(), winner, looser)

                session.copy(gameResult = Option.Some(result)) to result
            }
        }

}
