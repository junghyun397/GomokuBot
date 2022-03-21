package interact.message.graphics

import utility.MessagePublisher

open class TextBoardRenderer : BoardRenderer {

    override val styleName = "TEXT"

    override val sampleView =
        "```\n" +
       "  A B C D\n" +
       "4     O   4\n" +
       "3   X   X 3\n" +
       "2   O X   2\n" +
       "1 O   O   1\n" +
       "  A B C D\n" +
       "```"

    override fun attachBoard(messagePublisher: MessagePublisher) = TODO()

    override fun attachBoardWithButtons(messagePublisher: MessagePublisher) = TODO()

}
