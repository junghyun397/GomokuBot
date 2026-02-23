package core.assets

import renju.notation.Color
import renju.notation.Pos

fun validatePos(row: Int, col: Int): Boolean =
    row in 0 until Pos.BOARD_WIDTH && col in 0 until Pos.BOARD_WIDTH

val posList = (0 until Pos.BOARD_SIZE).map { Pos.fromIdx(it) }

fun buildBoard(history: List<Pos>): ByteArray {
    val field = ByteArray(Pos.BOARD_SIZE) { Color.emptyFlag() }

    history.forEachIndexed { index, pos ->
        field[pos.idx()] =
            if (index % 2 == 0) Color.Black.flag()
            else Color.White.flag()
    }

    return field
}
