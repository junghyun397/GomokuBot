package interact.message.graphics

import utility.MessagePublisher

enum class BoardStyle(val renderer: BoardRenderer) {
    IMAGE(ImageBoardRenderer()),
    TEXT(TextBoardRenderer()),
    SOLID_TEXT(SolidTextBoardRenderer()),
    UNICODE(UnicodeBoardRenderer())
}

interface BoardRenderer {

    val styleName: String

    val sampleView: String

    fun attachBoard(messagePublisher: MessagePublisher)

    fun attachBoardWithButtons(messagePublisher: MessagePublisher)

}
