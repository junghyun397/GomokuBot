package core.interact.message.graphics

import core.assets.*
import jrenju.Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.lang.clone
import utils.lang.toInputStream
import utils.structs.Either
import utils.structs.Option
import java.awt.Dimension
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

object ImageBoardRenderer : BoardRenderer, BoardRendererSample {

    override val styleShortcut = "A"

    override val styleName = "IMAGE"

    private const val POINT_SIZE = 60 // Must have 30 as a factor
    private val BOARD_SIZE = POINT_SIZE * Renju.BOARD_WIDTH()

    private const val COORDINATE_SIZE = POINT_SIZE / 2
    private const val COORDINATE_FONT_SIZE = POINT_SIZE / 3
    private const val COORDINATE_FONT = "Bitstream Charter"
    private const val COORDINATE_START_OFFSET = COORDINATE_SIZE + POINT_SIZE / 2
    private const val COORDINATE_PADDING = COORDINATE_SIZE / 5

    private val DIMENSION = (COORDINATE_SIZE * 2 + BOARD_SIZE).let { Dimension(it, it) }

    private const val LINE_WEIGHT = POINT_SIZE / 30
    private const val LINE_START_POS = COORDINATE_SIZE + POINT_SIZE / 2 - LINE_WEIGHT / 2
    private val LINE_END_POS = BOARD_SIZE - POINT_SIZE + LINE_WEIGHT

    private const val STONE_SIZE = POINT_SIZE - POINT_SIZE / 30
    private const val STONE_OFFSET = (POINT_SIZE - STONE_SIZE) / 2

    private const val BOARDER_SIZE = POINT_SIZE / 20

    private const val LATEST_MOVE_DOT_SIZE = POINT_SIZE / 5
    private const val LATEST_MOVE_DOT_OFFSET = (POINT_SIZE - LATEST_MOVE_DOT_SIZE) / 2

    private const val FORBIDDEN_DOT_SIZE = POINT_SIZE / 5
    private const val FORBIDDEN_DOT_OFFSET = (POINT_SIZE - FORBIDDEN_DOT_SIZE) / 2

    private const val HISTORY_FONT_SIZE = POINT_SIZE / 2
    private const val HISTORY_FONT = "Bitstream Charter"

    private val protoImage: BufferedImage by lazy {
        BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_INT_RGB).also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                color = COLOR_WOOD
                fillRect(0, 0, DIMENSION.width, DIMENSION.height)

                color = COLOR_BLACK
                for (idx in LINE_START_POS until LINE_START_POS + BOARD_SIZE step POINT_SIZE) {
                    fillRect(idx, LINE_START_POS, LINE_WEIGHT, LINE_END_POS)
                    fillRect(LINE_START_POS, idx, LINE_END_POS, LINE_WEIGHT)
                }

                val coordinateFont = Font(COORDINATE_FONT, Font.TRUETYPE_FONT, COORDINATE_FONT_SIZE)
                val fontMetrics = getFontMetrics(coordinateFont)
                font = coordinateFont

                val rowWidth = fontMetrics.stringWidth(Renju.BOARD_WIDTH().toString())

                for (idx in 0 until  Renju.BOARD_WIDTH()) {
                    val col = (65 + idx).toChar().toString()
                    val row = Renju.BOARD_WIDTH() - idx
                    val rowLeft = "%2d".format(row)
                    val rowRight = row.toString()

                    val colWidth = fontMetrics.stringWidth(col)
                    val rowLeftWidth = fontMetrics.stringWidth(rowLeft)
                    val rowRightWidth = fontMetrics.stringWidth(rowRight)

                    val colStartX = COORDINATE_START_OFFSET + idx * POINT_SIZE - colWidth / 2

                    drawString(col, colStartX, COORDINATE_FONT_SIZE)
                    drawString(col, colStartX, DIMENSION.height - COORDINATE_PADDING)

                    val rowStartY = COORDINATE_START_OFFSET + idx * POINT_SIZE + COORDINATE_PADDING

                    drawString(rowLeft, COORDINATE_PADDING + rowWidth - rowLeftWidth, rowStartY)
                    drawString(rowRight, DIMENSION.width - COORDINATE_PADDING - rowRightWidth, rowStartY)
                }

                fillOval(
                    DIMENSION.width / 2 - LINE_WEIGHT * 3,
                    DIMENSION.width / 2 - LINE_WEIGHT * 3,
                    LINE_WEIGHT * 6,
                    LINE_WEIGHT * 6,
                )

                dispose()
            }
        }
    }

    private data class BoardPos(val x: Int, val y: Int)

    private fun Pos.asBoardPos() =
        BoardPos(
            COORDINATE_SIZE + this.col() * POINT_SIZE,
            COORDINATE_SIZE + (Renju.BOARD_WIDTH() - this.row() - 1) * POINT_SIZE
        )

    override fun renderBoard(board: Board, history: Option<List<Pos?>>) =
        Either.Right(this.renderImageBoard(board, history))

    fun renderImageBoard(board: Board, history: Option<List<Pos?>>) =
        this.protoImage.clone().also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                board.boardField()
                    .withIndex()
                    .filter { it.value != Flag.FREE() }
                    .map { Pos.fromIdx(it.index).asBoardPos() to it.value }
                    .forEach { (pos, flag) -> when (flag) {
                        Flag.BLACK() -> {
                            color = COLOR_GREY
                            fillOval(pos.x + STONE_OFFSET, pos.y + STONE_OFFSET, STONE_SIZE, STONE_SIZE)

                            color = COLOR_BLACK
                            fillOval(
                                pos.x + STONE_OFFSET + BOARDER_SIZE,
                                pos.y + STONE_OFFSET + BOARDER_SIZE,
                                STONE_SIZE - 2 * BOARDER_SIZE,
                                STONE_SIZE - 2 * BOARDER_SIZE,
                            )
                        }
                        Flag.WHITE() -> {
                            color = COLOR_GREY
                            fillOval(pos.x + STONE_OFFSET, pos.y + STONE_OFFSET, STONE_SIZE, STONE_SIZE)

                            color = COLOR_WHITE
                            fillOval(
                                pos.x + STONE_OFFSET + BOARDER_SIZE,
                                pos.y + STONE_OFFSET + BOARDER_SIZE,
                                STONE_SIZE - 2 * BOARDER_SIZE,
                                STONE_SIZE - 2 * BOARDER_SIZE,
                            )
                        }
                        Flag.FORBIDDEN_33(), Flag.FORBIDDEN_44(), Flag.FORBIDDEN_6() -> {
                            color = COLOR_RED
                            fillOval(
                                pos.x + FORBIDDEN_DOT_OFFSET,
                                pos.y + FORBIDDEN_DOT_OFFSET,
                                LATEST_MOVE_DOT_SIZE,
                                LATEST_MOVE_DOT_SIZE,
                            )
                        }
                    } }

                history.fold(
                    onEmpty = {
                        board.latestPos()
                            .map { it.asBoardPos() }
                            .foreach { pos ->
                                color = if (board.color() == jrenju.notation.Color.BLACK()) COLOR_WHITE else COLOR_BLACK
                                fillOval(
                                    pos.x + LATEST_MOVE_DOT_OFFSET,
                                    pos.y + LATEST_MOVE_DOT_OFFSET,
                                    LATEST_MOVE_DOT_SIZE,
                                    LATEST_MOVE_DOT_SIZE
                                )
                            }
                    },
                    onDefined = { histories ->
                        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                        val historyFont = Font(HISTORY_FONT, Font.TRUETYPE_FONT, HISTORY_FONT_SIZE)
                        val fontMetrics = getFontMetrics(historyFont)
                        font = historyFont

                        histories
                            .withIndex()
                            .filter { it.value != null }
                            .map { it.index + 1 to it.value!!.asBoardPos() }
                            .forEach { (sequence, pos) ->
                                val textWidth = fontMetrics.stringWidth(sequence.toString())

                                color = if (sequence % 2 == 0) COLOR_BLACK else COLOR_WHITE
                                drawString(
                                    sequence.toString(),
                                    pos.x + POINT_SIZE / 2 - textWidth / 2,
                                    pos.y + POINT_SIZE / 2 + HISTORY_FONT_SIZE / 2 - HISTORY_FONT_SIZE / 5
                                )
                            }
                    }
                )

                dispose()
            }
        }.toInputStream() to "board-${System.currentTimeMillis()}.png"

}
