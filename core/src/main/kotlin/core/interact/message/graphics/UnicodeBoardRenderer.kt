package core.interact.message.graphics

class UnicodeBoardRenderer : TextBoardRenderer() {

    companion object : BoardRendererSample {

        override val styleShortcut = "D"

        override val styleName = "UNICODE TEXT"

        override val sampleView =
                "┏ＡＢＣＤ┓\n" +
                "４┏┳○┓４\n" +
                "３┣●╋●３\n" +
                "２┣○●┫２\n" +
                "１○┻○┛１\n" +
                "┗ＡＢＣＤ┛"

    }

}
