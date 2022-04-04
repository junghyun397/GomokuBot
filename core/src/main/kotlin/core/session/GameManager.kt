package core.session

import core.assets.User
import core.inference.B3nzeneClient
import core.session.entities.GameSession
import jrenju.Board
import jrenju.notation.Pos
import jrenju.rule.Renju
import utils.assets.LinuxTime
import utils.structs.Either
import utils.structs.Option

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
            emptyList(), LinuxTime()
        )

    fun validateMove(session: GameSession, pos: Pos): Option<RejectReason> =
        session.board.validateMove(pos.idx()).let {
            if (it.isDefined) when (it.get()) {
                jrenju.notation.RejectReason.EXIST() -> Option.Some(RejectReason.EXIST)
                jrenju.notation.RejectReason.FORBIDDEN() -> Option.Some(RejectReason.FORBIDDEN)
                else -> Option.Some(RejectReason.EXIST)
            }
            else Option.Empty
        }

    fun makePvEMove(session: GameSession, pos: Pos, b3nzeneClient: B3nzeneClient): Either<GameSession, Pair<GameSession, GameResult>> {
        val calculated = session.board.makeMove(pos.idx())
            .calculateL2Board()

        if (calculated.isEnd)
            return Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.WIN)
        else if (calculated.boardField().size == Renju.BOARD_LENGTH())
            return Either.Right(session.copy(board = calculated.calculateL3Board()) to GameResult.FULL)

        TODO()
    }

    fun makePvPMove(session: GameSession, pos: Pos): Either<GameSession, Pair<GameSession, GameResult>> {
        val calculated = session.board
            .makeMove(pos.idx())
            .calculateL2Board()
            .calculateL3Board()
            .calculateDeepL3Board()

        return if (calculated.isEnd)
            Either.Right(session.copy(board = calculated) to GameResult.WIN)
        else if (calculated.boardField().size == Renju.BOARD_LENGTH())
            Either.Right(session.copy(board = calculated) to GameResult.FULL)
        else
            Either.Left(session.copy(board = calculated))
    }

    fun undoMove(session: GameSession): Option<GameSession> = TODO()

}
