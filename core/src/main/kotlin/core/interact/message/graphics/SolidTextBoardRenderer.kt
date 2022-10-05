package core.interact.message.graphics

import renju.Board
import renju.notation.Pos
import utils.structs.Either
import utils.structs.Option

class SolidTextBoardRenderer : TextBoardRenderer() {

    override fun renderBoard(board: Board, history: Option<List<Pos?>>) =
        Either.Left("```\n${this.renderBoardText(board).replace(".", "·")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "C"

        override val styleName = "SOLID TEXT"

    }

}
