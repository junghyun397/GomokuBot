package core.interact.message.graphics

import jrenju.Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.lang.asString
import utils.structs.Either
import utils.structs.Option

object UnicodeBoardRenderer : BoardRenderer, BoardRendererSample {

    override val styleShortcut = "D"

    override val styleName = "UNICODE TEXT"

    private const val BLACK = '●'
    private const val WHITE = '○'

    private const val LAST_BLACK = '■'
    private const val LAST_WHITE = '□'

    private const val CORNER_T_L = '┏'
    private const val CORNER_T_R = '┓'
    private const val CORNER_B_L = '┗'
    private const val CORNER_B_R = '┛'

    private const val CORNER_T = '┳'
    private const val CORNER_B = '┻'

    private const val CORNER_L = '┣'
    private const val CORNER_R = '┫'

    private const val CROSS = '╋'

    private const val FORBIDDEN_33 = '３'
    private const val FORBIDDEN_44 = '４'
    private const val FORBIDDEN_6 = '６'

    private const val EMPTY = '　'
    private val MONOSPACE_DIGIT =
        (0xff10 .. 0xff19)
            .map { it.toChar() }

    private val MONOSPACE_ROW_L =
        (1 .. Renju.BOARD_WIDTH())
            .map { "${if (it / 10 == 0) EMPTY else MONOSPACE_DIGIT[it / 10]}${MONOSPACE_DIGIT[it % 10]}" }
    private val MONOSPACE_ROW_R =
        (1 .. Renju.BOARD_WIDTH())
            .map { "${if (it / 10 == 0) "" else MONOSPACE_DIGIT[it / 10]}${MONOSPACE_DIGIT[it % 10]}" }
    private val MONOSPACE_COL =
        (0xff21 until 0xff21 + Renju.BOARD_WIDTH())
            .map { it.toChar() }
            .asString()

    override fun renderBoard(board: Board, history: Option<List<Pos?>>) =
        Either.Left(this.renderUnicodeBoard(board))

    private fun renderUnicodeBoard(board: Board) =
        board.boardField()
            .mapIndexed { index, flag ->
                when (flag) {
                    Flag.BLACK() -> if (board.latestMove() == index) LAST_BLACK else BLACK
                    Flag.WHITE() -> if (board.latestMove() == index) LAST_WHITE else WHITE
                    Flag.FORBIDDEN_33() -> FORBIDDEN_33
                    Flag.FORBIDDEN_44() -> FORBIDDEN_44
                    Flag.FORBIDDEN_6() -> FORBIDDEN_6
                    else -> this.emptyCharacter(index)
                }
            }
            .chunked(Renju.BOARD_WIDTH())
            .mapIndexed { index, row -> "${MONOSPACE_ROW_L[index]} ${row.asString()} ${MONOSPACE_ROW_R[index]}\n" }
            .reduce { acc, s -> s + acc }
            .let { "＇　$MONOSPACE_COL\n$it　　$MONOSPACE_COL" }

    private fun emptyCharacter(index: Int) =
        when (index % Renju.BOARD_WIDTH()) {
            0 -> when (index / Renju.BOARD_WIDTH()) {
                0 -> CORNER_B_L
                Renju.BOARD_WIDTH() - 1 -> CORNER_T_L
                else -> CORNER_L
            }
            Renju.BOARD_WIDTH() - 1 -> when (index / Renju.BOARD_WIDTH()) {
                0 -> CORNER_B_R
                Renju.BOARD_WIDTH() - 1 -> CORNER_T_R
                else -> CORNER_R
            }
            else -> when (index / Renju.BOARD_WIDTH()) {
                0 -> CORNER_B
                Renju.BOARD_WIDTH() - 1 -> CORNER_T
                else -> CROSS
            }
        }

}
