package core.interact.message.graphics

import arrow.core.Either
import renju.Board
import renju.GameState
import renju.notation.Pos

open class TextBoardRenderer : BoardRenderer {

    protected fun renderBoardText(board: Board): String =
        board.toString()

    override fun renderBoard(state: GameState, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?) =
        Either.Left("```\n${this.renderBoardText(state.board).replace(".", " ")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "B"

        override val styleName = "TEXT"

    }

}
