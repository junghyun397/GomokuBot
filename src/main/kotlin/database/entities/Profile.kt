package database.entities

import utility.UserId

sealed interface Profile {

    val id: UserId
    val name: String

    val rating: Float
    val wins: Int
    val losses: Int

}