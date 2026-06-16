package renju

import renju.notation.Pos

data class GameState(val board: Board, val history: History) {

    fun play(pos: Pos?): GameState =
        this.copy(
            board = this.board.set(pos),
            history = this.history.play(pos),
        )

    fun undo(): GameState =
        if (this.history.isEmpty()) {
            this
        } else {
            this.copy(
                board = this.board.unset(this.history.last()),
                history = this.history.undo(),
            )
        }

}
