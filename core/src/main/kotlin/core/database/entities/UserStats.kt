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

    val isEmpty: Boolean get() =
        this.blackWins == 0 && this.blackLosses == 0 && this.blackDraws == 0
                && this.whiteWins == 0 && this.whiteLosses == 0 && this.whiteDraws == 0

    val totalWins: Int get() = this.blackWins + this.whiteWins

    val totalLosses: Int get() = this.blackLosses + this.whiteLosses

    val totalDraws: Int get() = this.blackDraws + this.whiteDraws

    override fun compareTo(other: UserStats) =
        when (val winsCompare = this.totalWins - other.totalWins) {
            0 -> when (val lossesCompare = other.totalLosses - this.totalLosses) {
                0 -> this.totalDraws - other.totalDraws
                else -> lossesCompare
            }
            else -> winsCompare
        }

    fun reversed(): UserStats =
        this.copy(
            blackWins = this.whiteLosses,
            blackLosses = this.whiteWins,
            blackDraws = this.whiteDraws,
            whiteWins = this.blackLosses,
            whiteLosses = this.blackWins,
            whiteDraws = this.blackDraws,
        )

}
