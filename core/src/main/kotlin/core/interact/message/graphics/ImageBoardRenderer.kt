package core.interact.message.graphics

import arrow.core.Either
import com.sksamuel.scrimage.AwtImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import renju.Board
import renju.native.RustyRenjuCApi
import renju.native.RustyRenjuImage
import renju.native.RustyRenjuImageApi
import renju.notation.Pos
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration
import javax.imageio.ImageIO

object ImageBoardRenderer : BoardRenderer, BoardRendererSample {

    override val styleShortcut = "A"

    override val styleName = "IMAGE"

    fun newFileName(): String =
        "board-${System.currentTimeMillis()}.png"

    fun newGifFileName(): String =
        "board-animated-${System.currentTimeMillis()}.gif"

    override fun renderBoard(
        board: Board,
        history: List<Pos?>,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?
    ) = runCatching {
        Either.Right(renderInputStream(board, history, historyRenderType, offers, blinds))
    }.getOrElse {
        Either.Left("```\n${board.toString().replace(".", " ")} ```")
    }

    private fun historyRenderOption(historyRenderType: HistoryRenderType): Byte =
        when (historyRenderType) {
            HistoryRenderType.LAST -> RustyRenjuImageApi.constants.rendererLast
            HistoryRenderType.RECENT -> RustyRenjuImageApi.constants.rendererPair
            HistoryRenderType.SEQUENCE -> RustyRenjuImageApi.constants.rendererSequence
        }

    private fun asMaybePosBuffer(history: List<Pos?>): ByteArray? =
        if (history.isEmpty()) {
            null
        } else {
            ByteArray(history.size) { index ->
                history[index]?.idx()?.toByte() ?: RustyRenjuCApi.constants.posNone
            }
        }

    private fun asPosBuffer(posSet: Set<Pos>?): ByteArray? {
        if (posSet.isNullOrEmpty()) {
            return null
        }

        return posSet
            .sortedBy { it.idx() }
            .map { it.idx().toByte() }
            .toByteArray()
    }

    private fun renderBytes(
        board: Board,
        history: List<Pos?>,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean,
    ): ByteArray {
        val actions = asMaybePosBuffer(history)
        val offerBuffer = asPosBuffer(offers)
        val blindBuffer = asPosBuffer(blinds)

        val rendered = RustyRenjuImageApi.lib.rusty_renju_image_render(
            RustyRenjuImageApi.constants.formatPng,
            1.0f,
            historyRenderOption(historyRenderType),
            enableForbiddenPoints,
            board.nativeHandle(),
            actions,
            actions?.size?.toLong() ?: 0L,
            offerBuffer,
            offerBuffer?.size?.toLong() ?: 0L,
            blindBuffer,
            blindBuffer?.size?.toLong() ?: 0L,
        )

        val pointer = rendered.ptr
            ?: throw IllegalStateException("Native renderer returned null pointer")

        val length = rendered.len.toLong().toInt()
        if (length <= 0) {
            throw IllegalStateException("Native renderer returned empty payload")
        }

        val bytes = pointer.getByteArray(0, length)

        RustyRenjuImageApi.lib.rusty_renju_image_free_byte_buffer(
            RustyRenjuImage.ByteBuffer().apply {
                ptr = rendered.ptr
                len = rendered.len
                write()
            }
        )

        return bytes
    }

    fun renderInputStream(
        board: Board,
        history: List<Pos?>,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean = true
    ): InputStream =
        ByteArrayInputStream(
            renderBytes(board, history, historyRenderType, offers, blinds, enableForbiddenPoints)
        )

    fun renderHistoryAnimation(history: List<Pos>): InputStream {
        val outputStream = ByteArrayOutputStream()

        val gifBuilder = StreamingGifWriter(Duration.ofSeconds(1), false, false)

        gifBuilder.prepareStream(outputStream, BufferedImage.TYPE_INT_ARGB).apply {
            var board: Board = Board.newBoard()

            history.forEachIndexed { index, pos ->
                board = board.set(pos)

                val frame = renderBytes(
                    board = board,
                    history = history.subList(0, index + 1),
                    historyRenderType = HistoryRenderType.SEQUENCE,
                    offers = null,
                    blinds = null,
                    enableForbiddenPoints = true,
                )

                val image = ImageIO.read(ByteArrayInputStream(frame))
                writeFrame(AwtImage(image).toImmutableImage())
            }

            close()
        }

        return ByteArrayInputStream(outputStream.toByteArray())
    }

}
