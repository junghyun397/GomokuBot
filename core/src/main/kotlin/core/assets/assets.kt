package core.assets

import jrenju.Board
import jrenju.BoardIO
import jrenju.`BoardIO$`
import jrenju.notation.Flag
import java.awt.Color

const val COLOR_NORMAL_HEX = 0x0091EA
const val COLOR_GREEN_HEX = 0x00C853
const val COLOR_RED_HEX = 0xD50000

val COLOR_WOOD = Color(242, 176, 109) // F2B06D
val COLOR_BLACK = Color(0, 0, 0) // 000000
val COLOR_WHITE = Color(255, 255, 255) // FFFFFF
val COLOR_GREY = Color(54, 57, 63) // 36393F
val COLOR_RED = Color(240, 71, 71) // F04747

const val UNICODE_CHECK = "\u2611\uFE0F" // ☑
const val UNICODE_CROSS = "\u274C" // ❌

const val UNICODE_BLACK_CIRCLE = "\u26AB" // ⚪
const val UNICODE_WHITE_CIRCLE = "\u26AA" // ⚫

const val UNICODE_CONSTRUCTION = "\uD83D\uDEA7" // 🚧
const val UNICODE_DARK_X = "\u2716" // ✖

const val UNICODE_ALARM_CLOCK = "\u23F0" // ⏰
const val UNICODE_ZAP = "\u26A1" // ⚡

const val UNICODE_MAILBOX = "\uD83D\uDCEB" // 📫

const val UNICODE_LEFT = "\u25C0" // ◀
const val UNICODE_DOWN = "\ud83d\udd3d" // 🔽
const val UNICODE_UP = "\ud83d\udd3c" // 🔼
const val UNICODE_RIGHT = "\u25b6" // ▶
const val UNICODE_FOCUS = "\u23fa" // ⏺

val anonymousUser = User(
    id = UserId(0),
    name = "Anon",
    nameTag = "Anon#0000",
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

fun Board.toBoardIO(): BoardIO.BoardToText = `BoardIO$`.`MODULE$`.BoardToText(this)
