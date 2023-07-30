package core.session.entities

import core.assets.Notation
import core.assets.User
import core.assets.aiUser
import renju.notation.Color
import renju.notation.Result
import utils.structs.Identifiable

sealed interface GameResult {

    val cause: Cause

    val message: String

    val winColorId: Short?

    enum class Cause(override val id: Short) : Identifiable {
        FIVE_IN_A_ROW(0), RESIGN(1), TIMEOUT(2), DRAW(3)
    }

    data class Win(override val cause: Cause, val winColor: Color, val winner: User, val loser: User) : GameResult {

        override val message get() = "$winner wins over $loser by $cause"

        override val winColorId = this.winColor.flag().toShort()

    }

    data object Full : GameResult {

        override val cause = Cause.DRAW

        override val message get() = "tie caused by full"

        override val winColorId = null

    }

    companion object {

        fun build(gameResult: Result, cause: Cause, blackUser: User?, whiteUser: User?): GameResult? =
            when (cause) {
                Cause.FIVE_IN_A_ROW, Cause.RESIGN, Cause.TIMEOUT -> when (gameResult.flag()) {
                    Notation.FlagInstance.BLACK() ->
                        Win(cause, Notation.Color.Black, blackUser ?: aiUser, whiteUser ?: aiUser)
                    Notation.FlagInstance.WHITE() ->
                        Win(cause, Notation.Color.White, whiteUser ?: aiUser, blackUser ?: aiUser)
                    else -> null
                }
                Cause.DRAW -> Full
            }

    }

}
