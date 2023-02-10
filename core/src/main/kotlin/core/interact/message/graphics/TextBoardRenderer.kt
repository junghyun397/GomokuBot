package core.interact.message.graphics

import core.assets.Notation
import renju.Board
import renju.notation.Pos
import utils.structs.Either

open class TextBoardRenderer : BoardRenderer {

    protected fun renderBoardText(board: Board): String =
        Notation.BoardIOInstance.buildBoardString(board, true)

    override fun renderBoard(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType) =
        Either.Left("```\n${this.renderBoardText(board).replace(".", " ")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "B"

        override val styleName = "TEXT"

    }

}
