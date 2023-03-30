import org.junit.Test
import renju.notation.Pos
import kotlin.math.abs
import kotlin.math.pow

class Playground {

    @Test
    fun sym() {
        val rs = calculateSymmetryMoves(Pos.fromCartesian("f7").get(), Pos.fromCartesian("h8").get(), Pos.fromCartesian("g6").get())

        println(rs)
    }

    private fun calculateSymmetryMoves(ref1: Pos, ref2: Pos, move: Pos): Set<Pos> {
        return if (ref1.row() == ref2.row() || ref1.col() == ref2.col()) {
            val reversedRow = ref1.row() + ref2.row() - move.row()
            val reversedCol = ref1.col() + ref2.col() - move.col()

            setOf(
                Pos(move.row(), reversedCol),
                Pos(reversedRow, move.col()),
                Pos(reversedRow, reversedCol)
            )
        }
        else {
            // y=ax+b
            val slope = abs(ref1.row() - ref2.row()).toDouble() / abs(ref1.col() - ref2.col()).toDouble()
            val intercept = (ref1.row() - slope * ref1.col())

            // x'= x-2a(ax-y+b)/(a^2+1)
            val reversedCol = (move.col() - (2 * slope * (slope * move.col() - move.row() + intercept)) / (slope.pow(2) + 1)).toInt()
            // y'= y+2(ax-y+b)/(a^2+1)
            val reversedRow = (move.row() + (2 * (slope * move.col() - move.row() + intercept)) / (slope.pow(2) + 1)).toInt()

            setOf(
                Pos(reversedRow, reversedCol)
            )
        }
    }

}
