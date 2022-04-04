package core.interact.message.graphics

import jrenju.L3Board
import utils.structs.Either
import java.io.File

enum class BoardStyle(val renderer: BoardRenderer, val sample: BoardRendererSample) {
    IMAGE(ImageBoardRenderer(), ImageBoardRenderer),
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

    fun renderBoard(board: L3Board): Either<String, File>

}
