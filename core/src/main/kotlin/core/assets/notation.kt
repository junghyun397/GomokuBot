package core.assets

import renju.Board
import renju.`BoardIO$`
import renju.`EmptyScalaBoard$`
import renju.FieldStatus
import renju.notation.Flag
import renju.notation.`Flag$`
import renju.notation.`Result$`
import renju.notation.Struct

fun intToStruct(raw: Int): Struct = Struct(raw)

fun byteToFlag(raw: Byte): Flag = Flag(raw)

operator fun Board.get(index: Int): FieldStatus = this.getFieldStatus(index)

typealias EmptyBoard = `EmptyScalaBoard$`

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
