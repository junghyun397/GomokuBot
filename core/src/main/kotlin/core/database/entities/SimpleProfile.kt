package core.database.entities

import core.assets.UserId

class SimpleProfile(
    val id: UserId,
    val name: String,

    val rating: Float,
    val wins: Int,
    val losses: Int
) : Comparable<SimpleProfile> {

    override fun compareTo(other: SimpleProfile): Int =
        wins - other.wins

    override fun equals(other: Any?): Boolean =
        this === other || (javaClass == other?.javaClass && id == (other as SimpleProfile).id)

    override fun hashCode(): Int =
        id.hashCode()

}

fun UserData.asSimpleProfile(): SimpleProfile = SimpleProfile(this.id, this.name, this.rating, this.wins, this.losses)
