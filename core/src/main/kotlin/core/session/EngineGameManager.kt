package core.session

import arrow.core.Either
import core.assets.User
import core.engine.EngineLevel
import core.engine.EngineProvider
import core.engine.MintakaIdleSession
import core.engine.MintakaServer
import core.session.entities.*
import renju.Board
import renju.GameState
import renju.History
import renju.notation.*
import kotlin.time.Duration.Companion.hours

object EngineGameManager {

    suspend fun create(mintakaServer: MintakaServer, user: User.Human, level: EngineLevel): EngineGameSession {
        val userColor = Color.random()

        val users = ColorContainer(User.GomokuBot, User.GomokuBot)
            .setColor(userColor, user)

        val state = when (userColor) {
            Color.BLACK -> GameState(Board.emptyBoard(), History.empty())
            Color.WHITE -> {
                val history = History
                    .empty()
                    .play(Pos.CENTER)

                GameState(Board.fromHistory(history), history)
            }
        }

        val mintakaSessionHandle = EngineProvider.createSession(mintakaServer, level, state)
        val engineState = Either.Right(mintakaSessionHandle.session.await())

        val session = EngineGameSession(
            context = GameSessionContext(
                id = SessionId.issue(),
                users = users,
                state = state,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(1.hours),
                ruleKind = Rule.RENJU,
            ),
            userColor = userColor,
            mintakaServer = mintakaServer,
            engineState = engineState,
            engineLevel = level,
            humanPlayer = user,
            recording = true,
        )

        return session
    }

    suspend fun play(session: EngineGameSession, move: Pos): EngineGameSession {
        val session = this.playMoveAndSyncEngine(session, move)

        if (session.gameResult != null) {
            return session
        }

        val handles = EngineProvider.launchSession(
            session.mintakaServer,
            session.mintakaSession as MintakaIdleSession,
            session.state.board.hashKey,
            false,
        )

        val bestMove = handles.bestMove.await()

        if (bestMove.move == null) {
            return session.copy(
                engineState = Either.Left(GameResult.Win(
                    GameResult.WinCause.RESIGN,
                    !session.state.board.playerColor
                ))
            )
        }

        return this.playMoveAndSyncEngine(session, bestMove.move)
    }

    fun undo(session: EngineGameSession): EngineGameSession {
        TODO()
    }

    enum class ResignCause(val cause: GameResult.WinCause) {
        RESIGN(GameResult.WinCause.RESIGN), TIMEOUT(GameResult.WinCause.TIMEOUT)
    }

    suspend fun resign(session: EngineGameSession, cause: ResignCause): EngineGameSession {
        val result = GameResult.Win(cause.cause, !session.state.board.playerColor)

        EngineProvider.deleteSession(session.mintakaServer, session.mintakaSession!!)

        val session = session.copy(
            engineState = Either.Left(result)
        )

        return session
    }

    private suspend fun playMoveAndSyncEngine(session: EngineGameSession, move: Pos): EngineGameSession {
        val positionHash = session.state.board.hashKey
        val state = session.state.play(move)
        val result = state.board.winner()

        if (result != null) {
            EngineProvider.deleteSession(session.mintakaServer, session.mintakaSession!!)

            return session.copy(
                context = session.context.next(state),
                engineState = Either.Left(result),
            )
        }

        val mintakaSession = EngineProvider.playSession(
            session.mintakaServer,
            session.mintakaSession as MintakaIdleSession,
            positionHash,
            move
        )

        if (mintakaSession.hash != state.board.hashKey) {
            throw IllegalStateException("desync")
        }

        return session.copy(
            context = session.context.next(state),
            engineState = Either.Right(mintakaSession),
        )
    }

}
