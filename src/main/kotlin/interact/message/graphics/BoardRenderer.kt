package interact.message.graphics

enum class Style(val renderer: BoardRenderer) {
    IMAGE(ImageBoardRenderer())
}

interface BoardRenderer {
}