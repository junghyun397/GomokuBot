package core.assets

import io.r2dbc.spi.Statement
import renju.notation.Flag
import utils.structs.Option
import java.awt.Color
import java.util.*

const val COLOR_NORMAL_HEX = 0x0091EA
const val COLOR_GREEN_HEX = 0x00C853
const val COLOR_RED_HEX = 0xD50000

val COLOR_WOOD = Color(242, 176, 109) // F2B06D
val COLOR_BLACK = Color(0, 0, 0) // 000000
val COLOR_WHITE = Color(255, 255, 255) // FFFFFF
val COLOR_GREY = Color(54, 57, 63) // 36393F
val COLOR_RED = Color(240, 0, 0) // F04747

const val UNICODE_CHECK = "\u2611\uFE0F" // ☑
const val UNICODE_CROSS = "\u274C" // ❌

const val UNICODE_BLACK_CIRCLE = "\u26AB" // ⚪
const val UNICODE_WHITE_CIRCLE = "\u26AA" // ⚫

const val UNICODE_CONSTRUCTION = "\uD83D\uDEA7" // 🚧
const val UNICODE_DARK_X = "\u2716" // ✖

const val UNICODE_ALARM_CLOCK = "\u23F0" // ⏰
const val UNICODE_ZAP = "\u26A1" // ⚡

const val UNICODE_MAILBOX = "\uD83D\uDCEB" // 📫

const val UNICODE_LEFT = "\u25C0\ufe0f" // ◀
const val UNICODE_DOWN = "\ud83d\udd3d" // 🔽
const val UNICODE_UP = "\ud83d\udd3c" // 🔼
const val UNICODE_RIGHT = "\u25b6\ufe0f" // ▶
const val UNICODE_FOCUS = "\u23fa\ufe0f" // ⏺

const val UNICODE_IMAGE = "\ud83d\uddbc" // 🖼
const val UNICODE_T = "\ud83c\uddf9" // 🇹
const val UNICODE_GEM = "\ud83d\udc8e" // 💎

const val UNICODE_LIGHT = "\ud83d\udca1" // 💡
const val UNICODE_NOTEBOOK = "\ud83d\udcd3" // 📓

const val UNICODE_MAG = "\ud83d\udd0d" // 🔍
const val UNICODE_BROOM = "\ud83e\uddf9" // 🧹
const val UNICODE_CABINET = "\ud83d\uddc4" // 🗄
const val UNICODE_RECYCLE = "\u267b" // ♻

const val UNICODE_SILHOUETTE = "\ud83d\udc64" // 👤
const val UNICODE_ID_CARD = "\ud83e\udeaa" // 🪪
const val UNICODE_LOCK = "\ud83d\udd12" // 🔒

const val UNICODE_SPEAKER = "\ud83d\udce2" // 📢

const val UNICODE_TROPHY = "\ud83c\udfc6" // 🏆
const val UNICODE_WHITE_FLAG = "\ud83c\udff3" // 🏳️
const val UNICODE_PENCIL = "\u270f\ufe0f" // ✏️

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
