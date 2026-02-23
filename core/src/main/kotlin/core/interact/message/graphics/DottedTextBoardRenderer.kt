package core.interact.message.graphics

import arrow.core.Either
import renju.Board
import renju.notation.Pos

class DottedTextBoardRenderer : TextBoardRenderer() {

    override fun renderBoard(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?) =
        Either.Left("```\n${this.renderBoardText(board).replace(".", "·")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "C"

        override val styleName = "DOTTED TEXT"

    }

}
