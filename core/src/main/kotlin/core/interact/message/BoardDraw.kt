package core.interact.message

import core.assets.User
import core.database.entities.GameRecord
import core.session.entities.GameSession
import renju.Board
import renju.GameState
import renju.notation.Color
import renju.notation.GameResult
import utils.tuple

sealed interface BoardDraw {

    data class Recipients(
        val player: Pair<User, Color>,
        val opponent: Pair<User, Color>
    )

    val recipients: Recipients

    val result: GameResult?

    val state: GameState

}

data class SessionBoardDraw<T : GameSession>(
    val session: T,
    private val anonymous: Boolean = false,
) : BoardDraw {

    override val recipients = BoardDraw.Recipients(
        player = tuple(this.session.player, this.session.state.board.playerColor),
        opponent = tuple(this.session.opponent, !this.session.state.board.playerColor),
    )

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

    override val recipients = BoardDraw.Recipients(
        player = tuple(this.gameRecord.users.black, Color.BLACK),
        opponent = tuple(this.gameRecord.users.white, Color.WHITE),
    )

    override val result = this.gameRecord.gameResult

}
