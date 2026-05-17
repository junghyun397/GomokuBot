package renju.notation

import core.assets.User
import utils.structs.Identifiable

sealed interface GameResult {

    val cause: Cause

    val message: String

    val winnerColor: Color?

    val winColorId: Short?

    fun flag(): Byte = winnerColor?.naiveFlag ?: Color.emptyFlag()

    enum class Cause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(0), RESIGN(1), TIMEOUT(2), DRAW(3)
    }

    data class FiveInRow(private val winColor: Color) : GameResult {

        override val cause = Cause.FIVE_IN_A_ROW

        override val message get() = "five in row by $winnerColor"

        override val winnerColor: Color = winColor

        override val winColorId = winnerColor.naiveFlag.toShort()

        fun winner(): Color = winnerColor

    }

    data class Win(override val cause: Cause, val winColor: Color, val winner: User, val loser: User) : GameResult {

        override val message get() = "$winner wins over $loser by $cause"

        override val winnerColor: Color = winColor

        override val winColorId = this.winColor.naiveFlag.toShort()

    }

    data object Full : GameResult {

        override val cause = Cause.DRAW

        override val message get() = "tie caused by full"

        override val winnerColor: Color? = null

        override val winColorId = null

    }

    companion object {

        @JvmStatic
        fun fromFlag(flag: Byte): GameResult =
            when (Color.from(flag)) {
                Color.Black -> FiveInRow(Color.Black)
                Color.White -> FiveInRow(Color.White)
                null -> Full
            }

        fun build(gameResult: GameResult, cause: Cause, blackUser: User?, whiteUser: User?): GameResult? =
            when (cause) {
                Cause.FIVE_IN_A_ROW, Cause.RESIGN, Cause.TIMEOUT -> when (gameResult.winnerColor) {
                    Color.Black ->
                        Win(cause, Color.Black, blackUser ?: User.GomokuBot, whiteUser ?: User.GomokuBot)
                    Color.White ->
                        Win(cause, Color.White, whiteUser ?: User.GomokuBot, blackUser ?: User.GomokuBot)
                    else -> null
                }
                Cause.DRAW -> Full
            }

    }

}
