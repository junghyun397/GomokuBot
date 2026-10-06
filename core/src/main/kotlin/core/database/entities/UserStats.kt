package core.database.entities

import core.assets.UserUid
import kotlin.time.Clock
import kotlin.time.Instant

data class UserStats(
    val userId: UserUid,

    val blackWins: Int = 0,
    val blackLosses: Int = 0,
    val blackDraws: Int = 0,

    val whiteWins: Int = 0,
    val whiteLosses: Int = 0,
    val whiteDraws: Int = 0,

    val lastUpdate: Instant = Clock.System.now()
) {

    val totalWins: Int get() = this.blackWins + this.whiteWins

    val totalLosses: Int get() = this.blackLosses + this.whiteLosses

    val totalDraws: Int get() = this.blackDraws + this.whiteDraws

}
