package renju.notation

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

data class Pos(private val value: Int) {

    constructor(row: Int, col: Int) : this(rowColToIdx(row, col))

    fun idx(): Int = value

    fun row(): Int = idxToRow(value)

    fun col(): Int = idxToCol(value)

    fun isValid(): Boolean = value in 0 until BOARD_SIZE

    override fun toString(): String =
        if (isValid()) "${('a'.code + col()).toChar()}${row() + 1}"
        else "invalid"

    companion object {

        const val BOARD_WIDTH: Int = 15
        const val BOARD_SIZE: Int = BOARD_WIDTH * BOARD_WIDTH
        const val BOARD_BOUND: Int = BOARD_WIDTH - 1

        val CENTER: Pos = fromIdx(BOARD_SIZE / 2)

        @JvmStatic
        fun isValidIdx(idx: Int): Boolean = idx in 0 until BOARD_SIZE

        @JvmStatic
        fun rowColToIdx(row: Int, col: Int): Int =
            row * BOARD_WIDTH + col

        @JvmStatic
        fun idxToRow(idx: Int): Int =
            idx / BOARD_WIDTH

        @JvmStatic
        fun idxToCol(idx: Int): Int =
            idx % BOARD_WIDTH

        @JvmStatic
        fun fromIdx(idx: Int): Pos =
            if (isValidIdx(idx)) Pos(idx)
            else throw IllegalArgumentException()

        @JvmStatic
        fun fromCartesian(source: String?): Option<Pos> {
            val normalized = source?.trim()?.lowercase() ?: return None
            if (normalized.length !in 2..3) {
                return None
            }

            val col = normalized.first().code - 'a'.code
            val row = (normalized.drop(1).toIntOrNull() ?: return None) - 1

            return if (row in 0 until BOARD_WIDTH && col in 0 until BOARD_WIDTH) Some(Pos(row, col))
            else None
        }

    }

}
