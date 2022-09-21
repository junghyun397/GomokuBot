package core.database.entities

import core.assets.UserUid
import utils.assets.LinuxTime

data class UserStats(
    val userId: UserUid,

    val blackWins: Int = 0,
    val blackLosses: Int = 0,
    val blackDraws: Int = 0,

    val whiteWins: Int = 0,
    val whiteLosses: Int = 0,
    val whiteDraws: Int = 0,

    val last_update: LinuxTime = LinuxTime.now()
) : Comparable<UserStats> {

    val totalWins get() = this.blackWins + this.whiteWins

    val totalLosses get() = this.blackLosses + this.whiteLosses

    val totalDraws get() = this.blackDraws + this.whiteDraws

    override fun compareTo(other: UserStats) =
        this.totalWins - other.totalWins

}
