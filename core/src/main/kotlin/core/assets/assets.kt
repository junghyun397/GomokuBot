package core.assets

import core.database.DatabaseConnection
import core.database.repositories.UserProfileRepository
import renju.notation.ForbiddenKind

suspend fun UserUid?.retrieveUserOrGomokuBot(connection: DatabaseConnection): User =
    this?.let {
        UserProfileRepository.retrieveUser(connection, it)
    } ?: User.GomokuBot

fun forbiddenKindToText(kind: ForbiddenKind?) =
    when (kind) {
        ForbiddenKind.DoubleThree -> "3-3"
        ForbiddenKind.DoubleFour -> "4-4"
        ForbiddenKind.Overline -> "≥6"
        null -> "UNKNOWN"
    }
