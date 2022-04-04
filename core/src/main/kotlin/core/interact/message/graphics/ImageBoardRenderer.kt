package core.interact.message.graphics

import jrenju.L3Board

class ImageBoardRenderer : BoardRenderer {

    override fun renderBoard(board: L3Board) = TODO()

    companion object : BoardRendererSample {

        override val styleShortcut = "A"

        override val styleName = "IMAGE"

        override val sampleView = ""

    }

}
