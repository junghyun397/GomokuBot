package interact.message.graphics

class UnicodeBoardRenderer : TextBoardRenderer() {

    override val styleName = "UNICODE TEXT"

    override val sampleView =
       "４┏┳○┓\n" +
       "３┣●╋●\n" +
       "２┣○●┫\n" +
       "１○┻○┛\n" +
       "┗ＡＢＣＤ"

}