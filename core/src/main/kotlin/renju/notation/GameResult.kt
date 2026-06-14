package renju.notation

import utils.Identifiable

sealed interface GameResult : Identifiable {

    val message: String

    val winner: Color?

    enum class WinCause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(1), RESIGN(2), TIMEOUT(3)
    }

    data class Win(val cause: WinCause, override val winner: Color) : GameResult {

        override val message get() = "$winner win by $cause"

        override val id = this.cause.id

    }

    data object Full : GameResult {

        override val message get() = "tie caused by full"

        override val winner = null

        override val id: Short = 0

    }

    companion object {

        fun fromId(id: Short, winner: Color?): GameResult? = when (winner) {
            null -> when (id) {
                Full.id -> Full
                else -> null
            }
            else -> when (id) {
                WinCause.FIVE_IN_A_ROW.id -> Win(WinCause.FIVE_IN_A_ROW, winner)
                WinCause.RESIGN.id -> Win(WinCause.RESIGN, winner)
                WinCause.TIMEOUT.id -> Win(WinCause.TIMEOUT, winner)
                else -> null
            }
        }

    }

}
