package core.interact.message.graphics

import com.sksamuel.scrimage.AwtImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import core.assets.*
import renju.Board
import renju.notation.Flag
import renju.notation.Pos
import renju.notation.Renju
import utils.lang.clone
import utils.lang.toInputStream
import utils.structs.Either
import java.awt.Dimension
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration

object ImageBoardRenderer : BoardRenderer, BoardRendererSample {

    override val styleShortcut = "A"

    override val styleName = "IMAGE"

    private const val POINT_SIZE = 60 // Must have 30 as a factor
    private val BOARD_WIDTH = POINT_SIZE * Renju.BOARD_WIDTH()

    private const val COORDINATE_SIZE = POINT_SIZE / 2
    private const val COORDINATE_FONT_SIZE = POINT_SIZE / 3
    private val COORDINATE_FONT = Font("Bitstream Charter", Font.TRUETYPE_FONT, COORDINATE_FONT_SIZE)
    private const val COORDINATE_START_OFFSET = COORDINATE_SIZE + POINT_SIZE / 2
    private const val COORDINATE_PADDING = COORDINATE_SIZE / 5

    private val DIMENSION = (COORDINATE_SIZE * 2 + BOARD_WIDTH).let { Dimension(it, it) }

    private const val LINE_WEIGHT = POINT_SIZE / 30
    private const val LINE_START_POS = COORDINATE_SIZE + POINT_SIZE / 2 - LINE_WEIGHT / 2
    private val LINE_END_POS = BOARD_WIDTH - POINT_SIZE + LINE_WEIGHT

    private const val STONE_SIZE = POINT_SIZE - POINT_SIZE / 30
    private const val STONE_OFFSET = (POINT_SIZE - STONE_SIZE) / 2

    private const val BORDER_SIZE = POINT_SIZE / 20

    private const val LATEST_MOVE_DOT_SIZE = POINT_SIZE / 5
    private const val LATEST_MOVE_DOT_OFFSET = (POINT_SIZE - LATEST_MOVE_DOT_SIZE) / 2

    private const val LATEST_MOVE_CROSS_SIZE = POINT_SIZE / 3
    private const val LATEST_MOVE_CROSS_WEIGHT = POINT_SIZE / 30
    private const val LATEST_MOVE_CROSS_HEIGHT_OFFSET = (POINT_SIZE - LATEST_MOVE_CROSS_WEIGHT) / 2
    private const val LATEST_MOVE_CROSS_WIDTH_OFFSET = (POINT_SIZE - LATEST_MOVE_CROSS_SIZE) / 2

    private const val FORBID_DOT_SIZE = POINT_SIZE / 5
    private const val FORBID_DOT_OFFSET = (POINT_SIZE - FORBID_DOT_SIZE) / 2

    private const val HISTORY_FONT_SIZE = POINT_SIZE / 2
    private val HISTORY_FONT = Font("Bitstream Charter" , Font.TRUETYPE_FONT, HISTORY_FONT_SIZE)

    private val prototypeImage: BufferedImage by lazy {
        BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_INT_RGB).also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                color = COLOR_WOOD
                fillRect(0, 0, DIMENSION.width, DIMENSION.height)

                color = COLOR_BLACK
                for (idx in LINE_START_POS until LINE_START_POS + BOARD_WIDTH step POINT_SIZE) {
                    fillRect(idx, LINE_START_POS, LINE_WEIGHT, LINE_END_POS)
                    fillRect(LINE_START_POS, idx, LINE_END_POS, LINE_WEIGHT)
                }

                val fontMetrics = getFontMetrics(COORDINATE_FONT)
                font = COORDINATE_FONT

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

    fun newFileName(): String =
        "board-${System.currentTimeMillis()}.png"

    fun newGifFileName(): String =
        "board-animated-${System.currentTimeMillis()}.gif"

    override fun renderBoard(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?) =
        Either.Right(this.renderInputStream(board, history, historyRenderType, offers, blinds))

    private fun renderBufferedImage(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?, enableForbiddenPoints: Boolean = true): BufferedImage =
        this.prototypeImage.clone().also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                for (idx in 0 until Renju.BOARD_SIZE()) {
                    when (val flag = board.field()[idx]) {
                        Flag.BLACK(), Flag.WHITE() -> {
                            val boardPos = Pos.fromIdx(idx).asBoardPos()

                            color = COLOR_GREY
                            fillOval(boardPos.x + STONE_OFFSET, boardPos.y + STONE_OFFSET, STONE_SIZE, STONE_SIZE)

                            color = if (flag == Flag.BLACK()) COLOR_BLACK else COLOR_WHITE
                            fillOval(
                                boardPos.x + STONE_OFFSET + BORDER_SIZE,
                                boardPos.y + STONE_OFFSET + BORDER_SIZE,
                                STONE_SIZE - 2 * BORDER_SIZE,
                                STONE_SIZE - 2 * BORDER_SIZE,
                            )
                        }
                        Flag.FORBIDDEN_33(), Flag.FORBIDDEN_44(), Flag.FORBIDDEN_6() -> {
                            val boardPos = Pos.fromIdx(idx).asBoardPos()

                            if (enableForbiddenPoints) {
                                color = COLOR_RED
                                fillOval(
                                    boardPos.x + FORBID_DOT_OFFSET,
                                    boardPos.y + FORBID_DOT_OFFSET,
                                    LATEST_MOVE_DOT_SIZE,
                                    LATEST_MOVE_DOT_SIZE,
                                )
                            }
                        }
                    }
                }

                offers?.forEach { pos ->
                    val boardPos = pos.asBoardPos()

                    color = COLOR_GREY_100
                    fillOval(boardPos.x + STONE_OFFSET, boardPos.y + STONE_OFFSET, STONE_SIZE, STONE_SIZE)

                    color = COLOR_BLACK_100
                    fillOval(
                        boardPos.x + STONE_OFFSET + BORDER_SIZE,
                        boardPos.y + STONE_OFFSET + BORDER_SIZE,
                        STONE_SIZE - 2 * BORDER_SIZE,
                        STONE_SIZE - 2 * BORDER_SIZE,
                    )
                }
                
                blinds?.forEach { pos ->
                    val boardPos = pos.asBoardPos()

                    color = COLOR_BLACK_100
                    fillRect(
                        boardPos.x, boardPos.y,
                        POINT_SIZE, POINT_SIZE
                    )
                }

                when (historyRenderType) {
                    HistoryRenderType.SEQUENCE -> {
                        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                        val fontMetrics = getFontMetrics(HISTORY_FONT)
                        font = HISTORY_FONT

                        for (idx in history.indices) {
                            val element = history[idx] ?: continue

                            val sequence = idx + 1
                            val pos = element.asBoardPos()

                            val textWidth = fontMetrics.stringWidth(sequence.toString())

                            color = if (sequence % 2 == 0) COLOR_BLACK else COLOR_WHITE
                            drawString(
                                sequence.toString(),
                                pos.x + POINT_SIZE / 2 - textWidth / 2,
                                pos.y + POINT_SIZE / 2 + HISTORY_FONT_SIZE / 2 - HISTORY_FONT_SIZE / 5
                            )
                        }
                    }
                    else -> {
                        board.lastPos()
                            .foreach { pos ->
                                val boardPos = pos.asBoardPos()

                                color = if (board.color() == Notation.Color.Black) COLOR_WHITE else COLOR_BLACK
                                fillOval(
                                    boardPos.x + LATEST_MOVE_DOT_OFFSET,
                                    boardPos.y + LATEST_MOVE_DOT_OFFSET,
                                    LATEST_MOVE_DOT_SIZE,
                                    LATEST_MOVE_DOT_SIZE
                                )
                            }

                        if (historyRenderType == HistoryRenderType.RECENT) {
                            history.getOrNull(history.lastIndex - 1)?.also { pos ->
                                val boardPos = pos.asBoardPos()

                                color = if (board.color() == Notation.Color.Black) COLOR_BLACK else COLOR_WHITE
                                fillRect(
                                    boardPos.x + LATEST_MOVE_CROSS_WIDTH_OFFSET,
                                    boardPos.y + LATEST_MOVE_CROSS_HEIGHT_OFFSET,
                                    LATEST_MOVE_CROSS_SIZE,
                                    LATEST_MOVE_CROSS_WEIGHT
                                )
                                fillRect(
                                    boardPos.x + LATEST_MOVE_CROSS_HEIGHT_OFFSET,
                                    boardPos.y + LATEST_MOVE_CROSS_WIDTH_OFFSET,
                                    LATEST_MOVE_CROSS_WEIGHT,
                                    LATEST_MOVE_CROSS_SIZE
                                )
                            }
                        }

                    }
                }

                dispose()
            }
        }

    fun renderInputStream(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?, enableForbiddenPoints: Boolean = true): InputStream =
        this.renderBufferedImage(board, history, historyRenderType, offers, blinds, enableForbiddenPoints).toInputStream()

    fun renderHistoryAnimation(history: List<Pos>): InputStream {
        val outputStream = ByteArrayOutputStream()

        val gifBuilder = StreamingGifWriter(Duration.ofSeconds(1), false, false)

        gifBuilder.prepareStream(outputStream, BufferedImage.TYPE_INT_ARGB).apply {
            var board: Board = Notation.EmptyBoard

            history.forEachIndexed { index, pos ->
                board = board.makeMove(pos)
                writeFrame(AwtImage(renderBufferedImage(board, history.subList(0, index + 1), HistoryRenderType.SEQUENCE, null, null)).toImmutableImage())
            }

            close()
        }

        return ByteArrayInputStream(outputStream.toByteArray())
    }

}
