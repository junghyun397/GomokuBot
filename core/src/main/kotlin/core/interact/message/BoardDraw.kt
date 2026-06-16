package core.interact.message

import core.assets.User
import core.database.entities.GameRecord
import core.session.entities.GameSession
import renju.Board
import renju.GameState
import renju.notation.ColorContainer
import renju.notation.GameResult

sealed interface BoardDraw {

    val users: ColorContainer<User>

    val result: GameResult?

    val state: GameState

}

data class SessionBoardDraw<T : GameSession>(
    val session: T,
    private val anonymous: Boolean = false,
) : BoardDraw {

    override val users =
        if (this.anonymous) this.session.users.map { it.anonymous() }
        else this.session.users

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

    override val users = this.gameRecord.users

    override val result = this.gameRecord.gameResult

}
