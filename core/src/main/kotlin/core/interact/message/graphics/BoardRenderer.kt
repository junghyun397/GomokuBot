package core.interact.message.graphics

import jrenju.Board
import utils.structs.Either
import java.io.InputStream

enum class BoardStyle(val renderer: BoardRenderer, val sample: BoardRendererSample) {
    IMAGE(ImageBoardRenderer, ImageBoardRenderer),
    TEXT(TextBoardRenderer(), TextBoardRenderer),
    SOLID_TEXT(SolidTextBoardRenderer(), SolidTextBoardRenderer),
    UNICODE(UnicodeBoardRenderer(), UnicodeBoardRenderer)
}

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

    val sampleView: String

}

sealed interface BoardRenderer {

    fun renderBoard(board: Board): Either<String, Pair<InputStream, String>>

}
