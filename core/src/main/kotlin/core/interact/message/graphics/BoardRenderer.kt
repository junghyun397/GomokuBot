package core.interact.message.graphics

import jrenju.Board
import jrenju.notation.Pos
import utils.structs.Either
import utils.structs.Option
import java.io.InputStream

enum class BoardStyle(val id: Int, val renderer: BoardRenderer, val sample: BoardRendererSample) {
    IMAGE(0, ImageBoardRenderer, ImageBoardRenderer),
    TEXT(1, TextBoardRenderer(), TextBoardRenderer),
    SOLID_TEXT(2, SolidTextBoardRenderer(), SolidTextBoardRenderer),
    UNICODE(3, UnicodeBoardRenderer, UnicodeBoardRenderer)
}

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

}

sealed interface BoardRenderer {

    fun renderBoard(board: Board, history: Option<List<Pos?>>): Either<String, Pair<InputStream, String>>

}
