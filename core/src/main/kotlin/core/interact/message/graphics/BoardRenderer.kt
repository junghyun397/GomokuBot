package core.interact.message.graphics

import arrow.core.Either
import renju.Board
import renju.notation.Pos
import utils.structs.Identifiable
import java.io.InputStream

enum class HistoryRenderType(override val id: Short) : Identifiable {
    LAST(0), RECENT(1), SEQUENCE(2)
}

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

}

sealed interface BoardRenderer {

    fun renderBoard(board: Board, history: List<Pos?>, historyRenderType: HistoryRenderType, offers: Set<Pos>?, blinds: Set<Pos>?): Either<String, InputStream>

}
