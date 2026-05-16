package core.interact.message.graphics

import arrow.core.Either
import com.sksamuel.scrimage.AwtImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import renju.Board
import renju.GameState
import renju.History
import renju.native.RustyRenjuImage
import renju.native.RustyRenjuImageApi
import renju.notation.Pos
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
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
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?
    ) = runCatching {
        Either.Right(renderInputStream(state, historyRenderType, offers, blinds))
    }.getOrElse {
        Either.Left("```\n${state.board.toString().replace(".", " ")} ```")
    }

    private fun historyRenderOption(historyRenderType: HistoryRenderType): Byte =
        when (historyRenderType) {
            HistoryRenderType.LAST -> RustyRenjuImageApi.constants.rendererLast
            HistoryRenderType.RECENT -> RustyRenjuImageApi.constants.rendererPair
            HistoryRenderType.SEQUENCE -> RustyRenjuImageApi.constants.rendererSequence
        }

    private fun asPosBuffer(posSet: Set<Pos>?): ByteArray? {
        if (posSet.isNullOrEmpty()) {
            return null
        }

        return posSet
            .sortedBy { it.idx }
            .map { it.idx.toByte() }
            .toByteArray()
    }

    private fun renderBytes(
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean,
    ): ByteArray {
        val actions = state.history.toMaybePosBuffer()
        val offerBuffer = asPosBuffer(offers)
        val blindBuffer = asPosBuffer(blinds)

        val rendered = RustyRenjuImageApi.lib.rusty_renju_image_render(
            RustyRenjuImageApi.constants.formatPng,
            1.0f,
            historyRenderOption(historyRenderType),
            enableForbiddenPoints,
            state.board.nativeHandle(),
            actions,
            state.history.moves.toLong(),
            offerBuffer,
            offerBuffer?.size?.toLong() ?: 0L,
            blindBuffer,
            blindBuffer?.size?.toLong() ?: 0L,
        )

        val pointer = rendered.ptr
        if (pointer == MemorySegment.NULL) {
            throw IllegalStateException("Native renderer returned null pointer")
        }

        if (rendered.len <= 0 || rendered.len > Int.MAX_VALUE.toLong()) {
            throw IllegalStateException("Native renderer returned empty payload")
        }
        val length = rendered.len.toInt()

        val bytes = pointer.reinterpret(length.toLong()).toArray(ValueLayout.JAVA_BYTE)

        RustyRenjuImageApi.lib.rusty_renju_image_free_byte_buffer(
            RustyRenjuImage.ByteBuffer(
                ptr = pointer,
                len = rendered.len,
            )
        )

        return bytes
    }

    fun renderInputStream(
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean = true
    ): InputStream =
        ByteArrayInputStream(
            renderBytes(state, historyRenderType, offers, blinds, enableForbiddenPoints)
        )

    fun renderHistoryAnimation(history: List<Pos>): InputStream {
        val outputStream = ByteArrayOutputStream()

        val gifBuilder = StreamingGifWriter(Duration.ofSeconds(1), false, false)

        gifBuilder.prepareStream(outputStream, BufferedImage.TYPE_INT_ARGB).apply {
            var state = GameState(Board.newBoard(), History.empty())

            history.forEachIndexed { index, pos ->
                state = state.play(pos)

                val frame = renderBytes(
                    state = state,
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
