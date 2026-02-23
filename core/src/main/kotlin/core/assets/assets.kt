package core.assets

import core.database.DatabaseConnection
import core.database.repositories.UserProfileRepository
import io.r2dbc.spi.Statement
import renju.notation.ForbiddenKind
import java.util.*

const val GENERIC_PLATFORM_ID: Short = 0

val anonymousUser = User(
    id = UserUid(UUID.fromString("d20fa7d1-8107-406d-8fe0-ff5335b2d559")),
    platform = GENERIC_PLATFORM_ID,
    givenId = UserId(0L),
    name = "Anon",
    uniqueName = "anon",
    announceId = null,
    profileURL = null,
)

val aiUser = anonymousUser.copy(
    name = "AI",
    uniqueName = "gomokubot"
)

suspend fun UserUid?.retrieveUserOrAiUser(connection: DatabaseConnection) =
    this?.let {
        UserProfileRepository.retrieveUser(connection, this)
    } ?: aiUser

fun forbiddenFlagToText(flag: Byte) =
    when (ForbiddenKind.fromFieldFlag(flag)) {
        ForbiddenKind.DoubleThree -> "3-3"
        ForbiddenKind.DoubleFour -> "4-4"
        ForbiddenKind.Overline -> "≥6"
        null -> "UNKNOWN"
    }

inline fun <reified T : Any> Statement.bindNullable(name: String, value: T?): Statement =
    when (value) {
        null -> bindNull(name, T::class.java)
        else -> bind(name, value)
    }
