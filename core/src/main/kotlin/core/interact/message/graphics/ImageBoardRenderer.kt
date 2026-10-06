package core.interact.message.graphics

import arrow.core.Either
import renju.GameState
import renju.native.RustyRenjuImage
import renju.notation.Pos
import java.io.ByteArrayInputStream
import java.io.InputStream

object ImageBoardRenderer : BoardRenderer, BoardRendererSample {

    override val styleShortcut = "A"

    override val styleName = "IMAGE"

    fun newFileName(): String =
        "board-${System.currentTimeMillis()}.png"

    override fun renderBoard(
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?
    ) = runCatching {
        Either.Right(this.renderInputStream(state, historyRenderType, offers, blinds))
    }.getOrElse {
        Either.Left("```\n${state.board.toString().replace(".", " ")} ```")
    }

    private fun historyRenderOption(historyRenderType: HistoryRenderType): Byte =
        when (historyRenderType) {
            HistoryRenderType.LAST -> RustyRenjuImage.rendererLast
            HistoryRenderType.RECENT -> RustyRenjuImage.rendererPair
            HistoryRenderType.SEQUENCE -> RustyRenjuImage.rendererSequence
        }

    private fun asPosBuffer(posSet: Set<Pos>?): IntArray? {
        if (posSet.isNullOrEmpty()) {
            return null
        }

        return posSet
            .sortedBy { it.idx }
            .map { it.idx }
            .toIntArray()
    }

    private fun renderBytes(
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean,
    ): ByteArray =
        RustyRenjuImage.renderPng(
            board = state.board.nativeHandle(),
            actions = state.history.toMaybePosBuffer(),
            option = this.historyRenderOption(historyRenderType),
            enableForbidden = enableForbiddenPoints,
            offers = this.asPosBuffer(offers),
            blinds = this.asPosBuffer(blinds),
        )

    fun renderInputStream(
        state: GameState,
        historyRenderType: HistoryRenderType,
        offers: Set<Pos>?,
        blinds: Set<Pos>?,
        enableForbiddenPoints: Boolean = true
    ): InputStream =
        ByteArrayInputStream(
            this.renderBytes(state, historyRenderType, offers, blinds, enableForbiddenPoints)
        )

}
