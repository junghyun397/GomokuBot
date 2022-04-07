@file:Suppress("unused")

package core.assets

import jrenju.notation.Flag
import java.awt.Color

const val COLOR_NORMAL_HEX = 0x0091EA
const val COLOR_GREEN_HEX = 0x00C853
const val COLOR_RED_HEX = 0xD50000

val COLOR_WOOD = Color(242, 176, 109) // F2B06D
val COLOR_BLACK = Color(0, 0, 0) // 000000
val COLOR_WHITE = Color(255, 255, 255) // FFFFFF
val COLOR_GREEN = Color(0, 200, 83) // 00C853
val COLOR_RED = Color(255, 0, 0) // FF0000

const val UNICODE_CHECK = "\u2611\uFE0F" // ☑
const val UNICODE_CROSS = "\u274C" // ❌

const val UNICODE_BLACK_CIRCLE = "\u26AB" // ⚪
const val UNICODE_WHITE_CIRCLE = "\u26AA" // ⚫

const val UNICODE_CONSTRUCTION = "\uD83D\uDEA7" // 🚧
const val UNICODE_DARK_X = "\u2716" // ✖

const val UNICODE_ALARM_CLOCK = "\u23F0" // ⏰
const val UNICODE_ZAP = "\u26A1" // ⚡

const val UNICODE_MAILBOX = "\uD83D\uDCEB" // 📫

fun forbiddenFlagToText(flag: Byte) =
    when (flag) {
        Flag.FORBIDDEN_33() -> "3-3"
        Flag.FORBIDDEN_44() -> "4-4"
        Flag.FORBIDDEN_6() -> "≥6"
        else -> "UNKNOWN"
    }
