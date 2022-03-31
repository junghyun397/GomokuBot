package core.database.entities

import core.assets.UserId

data class UserData(
    val id: UserId,
    val name: String,
    val profileURL: String,

    val rating: Float,
    val wins: Int,
    val losses: Int
)
