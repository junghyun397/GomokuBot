package core.interact.message.graphics

import core.assets.toBoardIO
import jrenju.Board
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.lang.asString
import utils.structs.Either
import utils.structs.Option

open class TextBoardRenderer : BoardRenderer {

    private val textBoardLineOffset = Renju.BOARD_WIDTH() * 2 + 9

    private val textBoardLineFactor = Renju.BOARD_WIDTH() * 2 + 6

    protected fun renderBoardText(board: Board) =
        board.toBoardIO().boardText()
            .toCharArray()
            .also { textBoard ->
                board.latestPos().foreach {
                    val idx = this.textBoardLineOffset + (Renju.BOARD_WIDTH_MAX_IDX() - it.row()) * this.textBoardLineFactor + it.col() * 2
                    textBoard[idx - 1] = '['
                    textBoard[idx + 1] = ']'
                }
            }
            .asString()

    override fun renderBoard(board: Board, history: Option<List<Pos>>) =
        Either.Left("```\n${this.renderBoardText(board).replace(".", " ")}```")

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
