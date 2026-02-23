package renju.notation

import core.assets.User
import core.assets.aiUser
import utils.structs.Identifiable

sealed class GameResult {

    abstract val cause: Cause

    abstract val message: String

    abstract val winColorId: Short?

    abstract fun flag(): Byte

    enum class Cause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(0), RESIGN(1), TIMEOUT(2), DRAW(3)
    }

    data class FiveInRow(private val winnerColor: Color) : GameResult() {

        override val cause = Cause.FIVE_IN_A_ROW

        override val message get() = "five in row by $winnerColor"

        override val winColorId = winnerColor.flag().toShort()

        fun winner(): Color = winnerColor

        override fun flag(): Byte = winnerColor.flag()

    }

    data class Win(override val cause: Cause, val winColor: Color, val winner: User, val loser: User) : GameResult() {

        override val message get() = "$winner wins over $loser by $cause"

        override val winColorId = this.winColor.flag().toShort()

        override fun flag(): Byte = winColor.flag()

    }

    data object Full : GameResult() {

        override val cause = Cause.DRAW

        override val message get() = "tie caused by full"

        override val winColorId = null

        override fun flag(): Byte = Color.emptyFlag()

    }

    companion object {

        @JvmStatic
        fun fromFlag(flag: Byte): GameResult =
            when (Color.fromFlag(flag)) {
                Color.Black -> FiveInRow(Color.Black)
                Color.White -> FiveInRow(Color.White)
                null -> Full
            }

        fun build(gameResult: GameResult, cause: Cause, blackUser: User?, whiteUser: User?): GameResult? =
            when (cause) {
                Cause.FIVE_IN_A_ROW, Cause.RESIGN, Cause.TIMEOUT -> when (gameResult.flag()) {
                    Color.Black.flag() ->
                        Win(cause, Color.Black, blackUser ?: aiUser, whiteUser ?: aiUser)
                    Color.White.flag() ->
                        Win(cause, Color.White, whiteUser ?: aiUser, blackUser ?: aiUser)
                    else -> null
                }
                Cause.DRAW -> Full
            }

    }

}
