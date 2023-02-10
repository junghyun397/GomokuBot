package core.interact.message.graphics

import renju.Board
import renju.notation.Pos
import utils.structs.Either
import java.io.InputStream

enum class HistoryRenderType {
    NONE, NUMBER, CROSS
}

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

}

sealed interface BoardRenderer {

    fun renderBoard(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType): Either<String, InputStream>

}
