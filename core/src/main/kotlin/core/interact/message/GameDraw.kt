package core.interact.message

import core.assets.User
import core.database.entities.GameRecord
import core.engine.EloRating
import core.session.entities.GameSession
import renju.Board
import renju.GameState
import renju.notation.Color
import renju.notation.ColorContainer
import renju.notation.GameResult
import utils.replaceIf

sealed interface GameDraw {

    val users: ColorContainer<User>

    val leaderColor: Color

    val result: GameResult?

}

sealed interface BoardDraw : GameDraw {

    val state: GameState

}

data class SessionBoardDraw<T : GameSession>(
    val session: T,
    private val anonymous: Boolean = false,
) : BoardDraw {

    override val users = this.session.users.replaceIf(this.anonymous) { it.map(User::anonymous) }

    override val leaderColor = this.session.state.board.playerColor

    override val result = this.session.gameResult

    override val state = this.session.state

}

data class GameRecordBoardDraw(
    val gameRecord: GameRecord,
    override val state: GameState,
) : BoardDraw {

    constructor(gameRecord: GameRecord) : this(
        gameRecord = gameRecord,
        state = GameState(Board.fromHistory(gameRecord.history), gameRecord.history),
    )

    override val leaderColor = Color.BLACK

    override val users = this.gameRecord.users

    override val result = this.gameRecord.gameResult

}

data class ResultDraw(
    override val users: ColorContainer<User>,
    override val leaderColor: Color,
    override val result: GameResult,
    val eloRating: Pair<EloRating, EloRating.Delta>?,
) : GameDraw
