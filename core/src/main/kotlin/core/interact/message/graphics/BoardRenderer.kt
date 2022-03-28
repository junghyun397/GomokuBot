package core.interact.message.graphics

import utils.monads.Either
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

    fun renderBoard(): Either<String, File>

}
