package core.database.entities

import core.assets.User

data class UserStats(
    val profile: User,

    val blackWins: Int,
    val blackLosses: Int,
    val blackDraws: Int,

    val whiteWins: Int,
    val whiteLosses: Int,
    val whiteDraws: Int,
) : Comparable<UserStats> {

    val totalWins get() = this.blackWins + this.whiteWins

    val totalLooses get() = this.blackLosses + this.whiteLosses

    val totalDraws get() = this.blackDraws + this.whiteDraws

    override fun compareTo(other: UserStats) =
        this.totalWins - other.totalWins

}
