package core.session

import core.assets.User
import core.inference.B3nzeneClient
import core.session.entities.GameSession
import jrenju.Board
import jrenju.notations.Pos
import jrenju.notations.Renju
import utils.monads.Either
import utils.monads.Option
import utils.values.LinuxTime

enum class FocusPolicy {
    INTELLIGENCE, FALLOWING, ONLY_COMMAND
}

enum class SweepPolicy {
    CLEAR_BODY, LEAVE
}

enum class ArchivePolicy {
    WITH_PROFILE, BY_ANONYMOUS, PRIVACY
}

enum class RejectReason {
    EXIST, FORBIDDEN
}

enum class GameResult {
    FULL, WIN, AI_WIN
}

object GameManager {

    fun generateSession(owner: User, opponent: Option<User>): GameSession =
        GameSession(
            owner, opponent,
            Board.newBoard(Pos.apply(7, 7).idx())
                .calculateL2Board()
                .calculateL3Board(),
            LinuxTime()
        )

    fun validateMove(session: GameSession, pos: Pos): Option<RejectReason> =
        session.board.validateMove(pos.idx()).let {
            if (it.isDefined) when (it.get()) {
                jrenju.notations.RejectReason.EXIST() -> Option.Some(RejectReason.EXIST)
                jrenju.notations.RejectReason.FORBIDDEN() -> Option.Some(core.session.RejectReason.FORBIDDEN)
                else -> Option.Some(RejectReason.EXIST)
            }
            else Option.Empty
        }

    fun makePvEMove(session: GameSession, pos: Pos, b3nzeneClient: B3nzeneClient): Either<GameSession, Pair<GameSession, GameResult>> {
        val calculated = session.board.makeMove(pos.idx())
            .calculateL2Board()

        if (calculated.isEnd)
            return Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.WIN)
        else if (calculated.boardField().size == Renju.BOARD_SIZE())
            return Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.FULL)

        TODO()
    }

    fun makePvPMove(session: GameSession, pos: Pos): Either<GameSession, Pair<GameSession, GameResult>> {
        val calculated = session.board.makeMove(pos.idx())
            .calculateL2Board()

        return if (calculated.isEnd)
            Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.WIN)
        else if (calculated.boardField().size == Renju.BOARD_SIZE())
            Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.FULL)
        else
            Either.Left(session.copy(board = calculated.calculateL3Board()))
    }

    fun undoMove(session: GameSession): Option<GameSession> = TODO()

}
