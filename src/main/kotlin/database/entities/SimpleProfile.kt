package database.entities

import utility.UserId

class SimpleProfile(
    override val id: UserId,
    override val name: String,

    override val rating: Float,
    override val wins: Int,
    override val losses: Int
) : Profile, Comparable<SimpleProfile> {

    override fun compareTo(other: SimpleProfile): Int {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleProfile

        if (id != other.id) return false
        if (wins != other.wins) return false
        if (losses != other.losses) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + wins
        result = 31 * result + losses
        return result
    }

}

fun UserData.toSimpleProfile(): SimpleProfile = SimpleProfile(this.id, this.name, this.rating, this.wins, this.losses)
