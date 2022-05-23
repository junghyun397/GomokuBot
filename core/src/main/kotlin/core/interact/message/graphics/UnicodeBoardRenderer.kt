package core.interact.message.graphics

class UnicodeBoardRenderer : TextBoardRenderer() {

    companion object : BoardRendererSample {

        override val styleShortcut = "D"

        override val styleName = "UNICODE TEXT"

        override val sampleView =
                "４┏┳○┓\n" +
                "３┣●╋●\n" +
                "２┣○●┫\n" +
                "１○┻○┛\n" +
                "┗ＡＢＣＤ "

    }

}
