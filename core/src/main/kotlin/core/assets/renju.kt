package core.assets

import renju.Board
import renju.BoardIO
import renju.`BoardIO$`
import renju.`EmptyScalaBoard$`
import renju.FieldStatus
import renju.notation.*

fun intToStruct(raw: Int): Struct = Struct(raw)

fun byteToFlag(raw: Byte): Flag = Flag(raw)

fun validatePos(row: Int, col: Int): Boolean =
    row in 0 until Renju.BOARD_WIDTH() && col in 0 until Renju.BOARD_WIDTH()

val posList = (0 until Renju.BOARD_SIZE()).map { Pos.fromIdx(it) }

operator fun Board.get(index: Int): FieldStatus = this.getFieldStatus(index)

typealias EmptyBoard = `EmptyScalaBoard$`

fun buildBoard(history: List<Pos>): ByteArray {
    val field = ByteArray(Renju.BOARD_SIZE()) { Notation.FlagInstance.EMPTY() }

    history.forEachIndexed { index, pos ->
        field[pos.idx()] =
            if (index % 2 == 0) Notation.FlagInstance.BLACK()
            else Notation.FlagInstance.WHITE()
    }

    return field
}

object Notation {

    val EmptyBoard: EmptyBoard = `EmptyScalaBoard$`.`MODULE$`

    val BoardIOInstance: `BoardIO$` = `BoardIO$`.`MODULE$`

    val FlagInstance: `Flag$` = `Flag$`.`MODULE$`

    val ResultInstance: `Result$` = `Result$`.`MODULE$`

    object Color {

        val Black: renju.notation.Color = renju.notation.Color.`Black$`.`MODULE$`

        val White: renju.notation.Color = renju.notation.Color.`White$`.`MODULE$`

    }

    object Direction {

        val X: renju.notation.Direction = renju.notation.Direction.`X$`.`MODULE$`

        val Y: renju.notation.Direction = renju.notation.Direction.`Y$`.`MODULE$`

        val IncreaseUp: renju.notation.Direction = renju.notation.Direction.`IncreaseUp$`.`MODULE$`

        val DescentUp: renju.notation.Direction = renju.notation.Direction.`DescentUp$`.`MODULE$`

    }

    object InvalidKind {

        val Exist: renju.notation.InvalidKind = renju.notation.InvalidKind.`Exist$`.`MODULE$`

        val Forbidden: renju.notation.InvalidKind = renju.notation.InvalidKind.`Forbidden$`.`MODULE$`

    }

}
