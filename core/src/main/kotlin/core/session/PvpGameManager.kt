package core.session

import core.BotConfig
import core.assets.User
import core.session.entities.*
import renju.Board
import renju.GameState
import renju.History
import renju.notation.Color
import renju.notation.ColorContainer
import renju.notation.GameResult
import renju.notation.Pos
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

object PvpGameManager {

    fun request(requester: User.Human, recipient: User.Human, rule: Rule): RequestSession {
        return RequestSession(
            id = SessionId.issue(),
            requester = requester,
            recipient = recipient,
            messageBufferKey = MessageBufferKey.issue(),
            rule = rule,
            expireDate = Clock.System.now() + BotConfig.requestExpireAfter
        )
    }

    fun create(requester: User.Human, recipient: User.Human, rule: Rule): GameSession {
        val requesterColor = Color.random()

        val users = when (requesterColor) {
            Color.BLACK -> ColorContainer(requester, recipient)
            Color.WHITE -> ColorContainer(recipient, requester)
        }

        val sessionId = SessionId.issue()

        fun newContext(history: History) =
            GameSessionContext(
                id = sessionId,
                users = users,
                state = GameState(Board.fromHistory(history), history),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(1.hours),
                ruleKind = rule,
            )

        val session = when (rule) {
            Rule.RENJU -> PvpGameSession(
                context = newContext(History.empty()),
                recording = true,
            )
            Rule.TARAGUCHI_10 -> TaraguchiSwapStageSession(
                context = newContext(History.of(listOf(Pos.CENTER))),
                isBranched = false
            )
            Rule.SOOSYRV_8 -> SoosyrvMoveStageSession(
                context = newContext(History.of(listOf(Pos.CENTER))),
                isBranched = false
            )
            else -> throw NotImplementedError()
        }

        return session
    }

    fun play(session: PvpGameSession, move: Pos): PvpGameSession {
        val state = session.state.play(move)

        return session.copy(
            context = session.context.next(state),
            gameResult = state.board.winner(),
        )
    }

    fun undo(session: PvpGameSession, user: User.Human): PvpGameSession {
        TODO()
    }

    fun resign(session: PvpGameSession, user: User.Human?): PvpGameSession {
        val winColor = !(session.users.color(user) ?: session.state.board.playerColor)

        val cause =
            if (user == null) GameResult.WinCause.TIMEOUT
            else GameResult.WinCause.RESIGN

        val result = GameResult.Win(cause, winColor)

        return session.copy(gameResult = result)
    }

    fun resign(session: OpeningSession, user: User.Human?): PvpGameSession {
        val winColor = !session.users.color(user ?: session.player)!!

        val cause =
            if (user == null) GameResult.WinCause.TIMEOUT
            else GameResult.WinCause.RESIGN

        val result = GameResult.Win(cause, winColor)

        return PvpGameSession(
            context = session.context,
            gameResult = result,
            recording = session.recording,
        )
    }

}
