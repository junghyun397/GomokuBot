package interact.message.graphics

import utility.MessagePublisher

enum class Style(val renderer: BoardRenderer) {
    IMAGE(ImageBoardRenderer()),
    TEXT(TextBoardRenderer()),
    TEXT_SOLID(TextBoardRenderer()),
    TEXT_UNICODE(TextBoardRenderer())
}

interface BoardRenderer {

    fun attachBoard(messagePublisher: MessagePublisher)

    fun attachBoardWithButtons(messagePublisher: MessagePublisher)

}
