package core.interact.message.graphics

import core.assets.toBoardIO
import jrenju.Board
import utils.structs.Either

open class TextBoardRenderer : BoardRenderer {

    override fun renderBoard(board: Board) =
        Either.Left("```\n${board.toBoardIO().boardText().replace(".", " ")}```")

    companion object : BoardRendererSample {

        override val styleShortcut = "B"

        override val styleName = "TEXT"

        override val sampleView = "```\n" +
                "  A B C D  \n" +
                "4     O   4\n" +
                "3   X   X 3\n" +
                "2   O X   2\n" +
                "1 O   O   1\n" +
                "  A B C D  \n" +
                "```"
    }

}
