package core.interact.message.graphics

import arrow.core.Either
import renju.GameState
import renju.notation.Pos

class DottedTextBoardRenderer : TextBoardRenderer() {

    override fun renderBoard(state: GameState, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?) =
        Either.Left("```\n${this.renderBoardText(state.board).replace(".", "·")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "C"

        override val styleName = "DOTTED TEXT"

    }

}
