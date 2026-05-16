package renju.notation

data class Pos(private val value: Int) {

    constructor(row: Int, col: Int) : this(rowColToIdx(row, col))

    val idx get(): Int = value

    val row get(): Int = idxToRow(value)

    val col get(): Int = idxToCol(value)

    fun isValid(): Boolean = value in 0 until BOARD_SIZE

    override fun toString(): String =
        if (isValid()) "${('a'.code + col).toChar()}${row + 1}"
        else "invalid"

    companion object {

        const val BOARD_WIDTH: Int = 15
        const val BOARD_SIZE: Int = BOARD_WIDTH * BOARD_WIDTH
        const val BOARD_BOUND: Int = BOARD_WIDTH - 1

        val CENTER: Pos = fromIdx(BOARD_SIZE / 2)

        fun isValidIdx(idx: Int): Boolean = idx in 0 until BOARD_SIZE

        fun rowColToIdx(row: Int, col: Int): Int =
            row * BOARD_WIDTH + col

        fun idxToRow(idx: Int): Int =
            idx / BOARD_WIDTH

        fun idxToCol(idx: Int): Int =
            idx % BOARD_WIDTH

        fun fromIdx(idx: Int): Pos =
            if (isValidIdx(idx)) Pos(idx)
            else throw IllegalArgumentException()

        fun fromCartesian(source: String?): Pos? {
            val normalized = source?.trim()?.lowercase() ?: return null

            if (normalized.length !in 2..3) {
                return null
            }

            val col = normalized.first().code - 'a'.code
            val row = (normalized.drop(1).toIntOrNull() ?: return null) - 1

            return if (row in 0 until BOARD_WIDTH && col in 0 until BOARD_WIDTH) Pos(row, col)
            else null
        }

    }

}
