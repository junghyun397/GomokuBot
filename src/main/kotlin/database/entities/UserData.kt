package database.entities

import utility.UserId

class UserData(
    override val id: UserId,
    override val name: String,
    val profileURL: String,

    override val rating: Float,
    override val wins: Int,
    override val losses: Int
) : Profile
