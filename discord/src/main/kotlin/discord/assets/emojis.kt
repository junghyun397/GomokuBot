@file:Suppress("unused")

package discord.assets

import core.assets.*
import core.session.entities.NavigationKind
import net.dv8tion.jda.api.entities.emoji.Emoji

val NAVIGATION_EMOJIS: List<Emoji> = NavigationKind.emojis.map { Emoji.fromUnicode(it) }

val EMOJI_CHECK = Emoji.fromUnicode(UNICODE_CHECK) // ☑
val EMOJI_CROSS = Emoji.fromUnicode(UNICODE_CROSS) // ❌

val EMOJI_BLACK_CIRCLE = Emoji.fromUnicode(UNICODE_BLACK_CIRCLE) // ⚪
val EMOJI_WHITE_CIRCLE = Emoji.fromUnicode(UNICODE_WHITE_CIRCLE) // ⚫

val EMOJI_CONSTRUCTION = Emoji.fromUnicode(UNICODE_CONSTRUCTION) // 🚧
val EMOJI_DARK_X = Emoji.fromUnicode(UNICODE_DARK_X) // ✖

val EMOJI_ALARM_CLOCK = Emoji.fromUnicode(UNICODE_ALARM_CLOCK) // ⏰
val EMOJI_ZAP = Emoji.fromUnicode(UNICODE_ZAP) // ⚡

val EMOJI_MAILBOX = Emoji.fromUnicode(UNICODE_MAILBOX) // 📫

val EMOJI_LEFT = Emoji.fromUnicode(UNICODE_LEFT) // ◀
val EMOJI_DOWN = Emoji.fromUnicode(UNICODE_DOWN) // 🔽
val EMOJI_UP = Emoji.fromUnicode(UNICODE_UP) // 🔼
val EMOJI_RIGHT = Emoji.fromUnicode(UNICODE_RIGHT) // ▶
val EMOJI_FOCUS = Emoji.fromUnicode(UNICODE_FOCUS) // ⏺

val EMOJI_IMAGE = Emoji.fromUnicode(UNICODE_IMAGE) // 🖼
val EMOJI_T = Emoji.fromUnicode(UNICODE_T) // 🇹
val EMOJI_GEM = Emoji.fromUnicode(UNICODE_GEM) // 💎

val EMOJI_LIGHT = Emoji.fromUnicode(UNICODE_LIGHT)
val EMOJI_NOTEBOOK = Emoji.fromUnicode(UNICODE_NOTEBOOK)

val EMOJI_MAG = Emoji.fromUnicode(UNICODE_MAG) // 🔍
val EMOJI_BROOM = Emoji.fromUnicode(UNICODE_BROOM) // 🧹
val EMOJI_CABINET = Emoji.fromUnicode(UNICODE_CABINET) // 🗄
val EMOJI_RECYCLE = Emoji.fromUnicode(UNICODE_RECYCLE) // ♻

val EMOJI_SILHOUETTE = Emoji.fromUnicode(UNICODE_SILHOUETTE) // 👤
val EMOJI_ID_CARD = Emoji.fromUnicode(UNICODE_ID_CARD) // 🪪
val EMOJI_LOCK = Emoji.fromUnicode(UNICODE_LOCK) // 🔒

val EMOJI_SPEAKER = Emoji.fromUnicode(UNICODE_SPEAKER) // 📢

val EMOJI_TROPHY = Emoji.fromUnicode(UNICODE_TROPHY) // 🏆
val EMOJI_WHITE_FLAG = Emoji.fromUnicode(UNICODE_WHITE_FLAG) // 🏳️
val EMOJI_PENCIL = Emoji.fromUnicode(UNICODE_PENCIL) // ✏️
