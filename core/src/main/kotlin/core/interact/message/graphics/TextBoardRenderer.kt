package core.interact.message.graphics

import jrenju.BoardTransform
import jrenju.L3Board
import utils.structs.Either

open class TextBoardRenderer : BoardRenderer {

    override fun renderBoard(board: L3Board) =
        Either.Left("```\n${BoardTransform.buildBoardText(board.boardField()).replace(".", " ")}```")

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
