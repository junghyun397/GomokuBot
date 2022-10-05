package core.interact.message.graphics

import renju.Board
import renju.notation.Pos
import utils.structs.Either
import utils.structs.Option
import java.io.InputStream

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

}

sealed interface BoardRenderer {

    fun renderBoard(board: Board, history: Option<List<Pos?>>): Either<String, InputStream>

}
