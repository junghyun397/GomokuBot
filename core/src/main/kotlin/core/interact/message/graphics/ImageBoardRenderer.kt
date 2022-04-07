package core.interact.message.graphics

import core.assets.*
import jrenju.L3Board
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.rule.Renju
import utils.structs.Either
import java.awt.Dimension
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

class ImageBoardRenderer : BoardRenderer {

    private val protoImage: BufferedImage by lazy {
        BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_INT_RGB).also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                color = COLOR_WOOD
                fillRect(0, 0, DIMENSION.width, DIMENSION.height)

                color = COLOR_BLACK
                for (idx in LINE_START_POS .. LINE_START_POS + BOARD_SIZE step POINT_SIZE) {
                    fillRect(idx, LINE_START_POS, LINE_WEIGHT, LINE_END_POS)
                    fillRect(LINE_START_POS, idx, LINE_END_POS, LINE_WEIGHT)
                }

                val coordinateFont = Font(COORDINATE_FONT, Font.TRUETYPE_FONT, COORDINATE_FONT_SIZE)
                val fontMetrics = getFontMetrics(coordinateFont)
                font = coordinateFont

                val colWidth = fontMetrics.stringWidth(Renju.BOARD_WIDTH().toString())

                for (idx in 0 until  Renju.BOARD_WIDTH()) {
                    val row = (65 + idx).toChar().toString()
                    val col = 15 - idx
                    val colLeft = "%2d".format(col)
                    val colRight = col.toString()

                    val rowWidth = fontMetrics.stringWidth(row)
                    val colLeftWidth = fontMetrics.stringWidth(colLeft)
                    val colRightWidth = fontMetrics.stringWidth(colRight)

                    val rowStartX = COORDINATE_START_OFFSET + idx * POINT_SIZE - rowWidth / 2

                    drawString(row, rowStartX, COORDINATE_FONT_SIZE)
                    drawString(row, rowStartX, DIMENSION.height - COORDINATE_PADDING)

                    val colStartY = COORDINATE_START_OFFSET + idx * POINT_SIZE + COORDINATE_PADDING

                    drawString(colLeft, COORDINATE_PADDING + colWidth - colLeftWidth, colStartY)
                    drawString(colRight, DIMENSION.width - COORDINATE_PADDING - colRightWidth, colStartY)
                }

                dispose()
            }
        }
    }

    private fun BufferedImage.clone(): BufferedImage =
        BufferedImage(this.colorModel, this.copyData(null), this.colorModel.isAlphaPremultiplied, null)

    private fun BufferedImage.toFile(): Pair<InputStream, String> {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(this, "png", outputStream)
        return ByteArrayInputStream(outputStream.toByteArray()) to "board-${System.currentTimeMillis()}.png"
    }

    private data class BoardPos(val x: Int, val y: Int)

    private fun Pos.asBoardPos() =
        BoardPos(
            COORDINATE_SIZE + this.row() * POINT_SIZE,
            COORDINATE_SIZE + (Renju.BOARD_WIDTH() - this.col() - 1) * POINT_SIZE
        )

    override fun renderBoard(board: L3Board) =
        Either.Right(
            this.protoImage.clone().also { image ->
                image.createGraphics().apply {
                    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                    board.boardField()
                        .withIndex()
                        .filter { it.value != Flag.FREE() }
                        .map { Pos.fromIdx(it.index).asBoardPos() to it.value }
                        .forEach { when(it.second) {
                            Flag.BLACK() -> {
                                color = COLOR_BLACK
                                fillOval(it.first.x, it.first.y, POINT_SIZE, POINT_SIZE)
                            }
                            Flag.WHITE() -> {
                                color = COLOR_BLACK
                                fillOval(it.first.x, it.first.y, POINT_SIZE, POINT_SIZE)

                                color = COLOR_WHITE
                                fillOval(
                                    it.first.x + LINE_WEIGHT,
                                    it.first.y + LINE_WEIGHT,
                                    POINT_SIZE - 2 * LINE_WEIGHT,
                                    POINT_SIZE - 2 * LINE_WEIGHT,
                                )
                            }
                            Flag.FORBIDDEN_33(), Flag.FORBIDDEN_44(), Flag.FORBIDDEN_6() -> {
                                color = COLOR_RED
                                fillOval(
                                    it.first.x + FORBIDDEN_DOT_OFFSET,
                                    it.first.y + FORBIDDEN_DOT_OFFSET,
                                    LATEST_MOVE_DOT_SIZE,
                                    LATEST_MOVE_DOT_SIZE
                                )
                            }
                        } }

                    val pos = board.latestPos().asBoardPos()

                    color = if (board.color() == jrenju.notation.Color.BLACK()) COLOR_BLACK else COLOR_WHITE
                    fillOval(
                        pos.x + LATEST_MOVE_DOT_OFFSET,
                        pos.y + LATEST_MOVE_DOT_OFFSET,
                        LATEST_MOVE_DOT_SIZE,
                        LATEST_MOVE_DOT_SIZE
                    )

                    dispose()
                }
            }.toFile()
        )


    companion object : BoardRendererSample {

        override val styleShortcut = "A"

        override val styleName = "IMAGE"

        override val sampleView = ""

        private const val POINT_SIZE: Int = 60 // Must have 30 as a factor
        private const val COORDINATE_SIZE: Int = POINT_SIZE / 2
        private const val COORDINATE_FONT_SIZE: Int = POINT_SIZE / 3
        private val BOARD_SIZE: Int = POINT_SIZE * Renju.BOARD_WIDTH()

        private val DIMENSION = (COORDINATE_SIZE * 2 + BOARD_SIZE).let { Dimension(it, it) }

        private const val LINE_WEIGHT: Int = POINT_SIZE / 30
        private const val LINE_START_POS = COORDINATE_SIZE + POINT_SIZE / 2
        private val LINE_END_POS = BOARD_SIZE - POINT_SIZE + LINE_WEIGHT

        private const val COORDINATE_FONT = "Bitstream Charter"
        private const val COORDINATE_START_OFFSET = COORDINATE_SIZE + POINT_SIZE / 2
        private const val COORDINATE_PADDING = COORDINATE_SIZE / 5

        private const val LATEST_MOVE_DOT_SIZE = POINT_SIZE / 5
        private const val LATEST_MOVE_DOT_OFFSET = (POINT_SIZE - LATEST_MOVE_DOT_SIZE) / 2

        private const val FORBIDDEN_DOT_SIZE = POINT_SIZE / 5
        private const val FORBIDDEN_DOT_OFFSET = (POINT_SIZE - FORBIDDEN_DOT_SIZE) / 2

        fun mapHistories(board: L3Board, history: Array<Pos>): Pair<InputStream, String> = TODO()

    }

}
