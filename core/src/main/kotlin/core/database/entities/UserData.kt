package core.database.entities

import utils.values.UserId

class UserData(
    val id: UserId,
    val name: String,
    val profileURL: String,

    val rating: Float,
    val wins: Int,
    val losses: Int
)
