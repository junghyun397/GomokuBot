package core.interact.message

import core.assets.User
import core.database.entities.GameRecord
import core.engine.EloRating
import core.session.entities.GameSession
import renju.GameState
import renju.notation.Color
import renju.notation.ColorContainer
import renju.notation.GameResult
import renju.notation.map
import utils.replaceIf

sealed interface GameParticipants {

    val users: ColorContainer<User>

    val leaderColor: Color

}

sealed interface GameDraw : GameParticipants {

    val result: GameResult?

}

data class BoardDraw(
    override val users: ColorContainer<User>,
    override val leaderColor: Color,
    override val result: GameResult?,
    val state: GameState
) : GameDraw

fun GameSession.buildBoardDraw(anonymous: Boolean = false) =
    BoardDraw(
        users = this.users.replaceIf(anonymous) { it.map(User::anonymous) },
        leaderColor = this.state.board.playerColor,
        result = this.gameResult,
        state = this.state,
    )

fun GameRecord.buildBoardDraw(state: GameState) =
    BoardDraw(
        users = this.users,
        leaderColor = Color.BLACK,
        result = this.gameResult,
        state = state,
    )

data class ResultDraw(
    override val users: ColorContainer<User>,
    override val leaderColor: Color,
    override val result: GameResult,
    val eloRating: Pair<EloRating, EloRating.Delta>?,
) : GameDraw
