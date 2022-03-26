package core.interact.message.graphics

class SolidTextBoardRenderer : TextBoardRenderer() {

    companion object : BoardRendererSample {

        override val styleShortcut = "C"

        override val styleName = "SOLID TEXT"

        override val sampleView = "```\n" +
                "  A B C D\n" +
                "4 · · O · 4\n" +
                "3 · X · X 3\n" +
                "2 · O X · 2\n" +
                "1 O · O · 1\n" +
                "  A B C D\n" +
                "```"

    }

}
