package core.session

import core.assets.User
import core.assets.aiUser
import core.inference.InferenceManager
import core.session.entities.*
import jrenju.Board
import jrenju.`EmptyBoard$`
import jrenju.notation.Color
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

    class Win(val winColor: Enumeration.Value, val winner: User, val looser: User) : GameResult
    object Full : GameResult

}

object GameManager {

    fun generatePvpSession(owner: User, opponent: User): GameSession =
        PvpGameSession(
            owner = owner,
            opponent = opponent,
            ownerHasBlack = Random(System.currentTimeMillis()).nextBoolean(),
            board = `EmptyBoard$`.`MODULE$`,
            history = emptyList(),
            messageBufferKey = generateMessageBufferKey(owner),
            expireDate = LinuxTime(),
        )

    fun generateAiSession(owner: User): GameSession {
        val ownerHasBlack = Random(System.currentTimeMillis()).nextBoolean()

        val board = if (ownerHasBlack) `EmptyBoard$`.`MODULE$` else Board.newBoard()

        return AiGameSession(
            owner = owner,
            opponent = aiUser,
            solution = Option.Empty,
            ownerHasBlack = ownerHasBlack,
            board = board,
            history = emptyList(),
            messageBufferKey = generateMessageBufferKey(owner),
            expireDate = LinuxTime(),
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
            .calculateL2Board()
            .calculateL3Board()
            .calculateDeepL3Board()

        if (session.board.moves() + 1 >= Renju.BOARD_LENGTH())
            return session.nextWith(thenBoard, pos, Option.Some(GameResult.Full))

        if (thenBoard.isEnd) {
            val (winner, looser) = if (session.ownerHasBlack && thenBoard.color() == Color.BLACK())
                session.owner to session.opponent
            else
                session.opponent to session.owner

            return session.nextWith(thenBoard, pos, Option.Some(GameResult.Win(thenBoard.color(), winner, looser)))
        }

        return session.nextWith(thenBoard, pos, Option.Empty)
    }

    fun makeAiMove(session: AiGameSession, latestMove: Pos): AiGameSession {
        val solution = session.solution
            .flatMap { solutionNode ->
                solutionNode.child()[latestMove.idx()].fold(
                    { Option.Empty },
                    { Option.Some(it) }
                )
            }

        val (aiMove, solutionNode) = solution.fold(
            onDefined = { when(it) {
                is SolutionNode -> it.idx() to Option.Some(it)
                else -> it.idx() to Option.Empty
            } },
            onEmpty = { when (val aiSolution = InferenceManager.retrieveAiMove(session.board, Pos.fromIdx(session.board.latestMove()))) {
                is SolutionNode -> aiSolution.idx() to Option.Some(aiSolution)
                else -> aiSolution.idx() to Option.Empty
            } },
        )

        val thenBoard = session.board.makeMove(aiMove)
            .calculateL2Board()
            .calculateL3Board()
            .calculateDeepL3Board()

        return if (thenBoard.isEnd) {
            val gameResult = GameResult.Win(thenBoard.color(), aiUser, session.owner)
            session.copy(
                board = thenBoard,
                gameResult = Option.Some(gameResult),
                history = session.history + Pos.fromIdx(aiMove),
                solution = solutionNode
            )
        } else session.copy(board = thenBoard, history = session.history + Pos.fromIdx(aiMove))
    }

}
