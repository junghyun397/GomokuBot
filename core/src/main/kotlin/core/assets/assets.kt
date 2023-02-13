package core.assets

import io.r2dbc.spi.Statement
import renju.notation.Flag
import utils.structs.Option
import java.util.*

const val GENERIC_PLATFORM_ID: Short = 0

val anonymousUser = User(
    id = UserUid(UUID.fromString("d20fa7d1-8107-406d-8fe0-ff5335b2d559")),
    platform = GENERIC_PLATFORM_ID,
    givenId = UserId(0L),
    name = "Anon",
    nameTag = "Anon#0000",
    announceId = null,
    profileURL = null,
)

val aiUser = anonymousUser.copy(
    name = "AI",
    nameTag = "AI#0042"
)

fun forbiddenFlagToText(flag: Byte) =
    when (flag) {
        Flag.FORBIDDEN_33() -> "3-3"
        Flag.FORBIDDEN_44() -> "4-4"
        Flag.FORBIDDEN_6() -> "≥6"
        else -> "UNKNOWN"
    }

inline fun <reified T : Any> Statement.bindNullable(name: String, value: T?): Statement =
    when (value) {
        null -> bindNull(name, T::class.java)
        else -> bind(name, value)
    }

fun <T> scala.Option<T>.toOption(): Option<T> =
    when (this.isDefined) {
        true -> Option.Some(this.get())
        else -> Option.Empty
    }
