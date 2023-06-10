package core.session.entities

import core.BotConfig
import core.assets.*
import core.inference.FocusSolver
import core.interact.i18n.Language
import core.interact.message.MessagingServiceImpl
import core.interact.message.SettingMapping
import renju.notation.Renju
import utils.assets.LinuxTime
import utils.assets.toBytes
import utils.structs.Identifiable
import utils.structs.Option
import utils.structs.find

enum class NavigationKind(override val id: Short, val range: IntRange, val navigators: Set<String>) : Identifiable {

    BOARD(0, 0 until Renju.BOARD_SIZE(), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    SETTINGS(1, 0 .. SettingMapping.map.size, setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ABOUT(2, 0 .. MessagingServiceImpl.aboutRenjuDocument[Language.ENG.container]!!.first.size, setOf(UNICODE_LEFT, UNICODE_RIGHT));

    companion object {

        val navigators: Set<String> = NavigationKind.values()
            .map { it.navigators }
            .reduce { acc, kind -> acc + kind }

    }

}

sealed interface NavigationState : Expirable {

    val kind: NavigationKind

    val page: Int

}

data class PageNavigationState(
    private val messageRef: MessageRef,
    override val kind: NavigationKind,
    override val page: Int,
    override val expireDate: LinuxTime,
) : NavigationState {

    fun encodeToColor(base: Int): Int =
        encodeToColor(base, kind, page)

    companion object {

        fun encodeToColor(base: Int, kind: NavigationKind, page: Int): Int {
            val baseBytes = base.toBytes()
                .drop(1)
                .map { it.toUByte().toInt() }

            val headByte: Int = page shr 1
            val tailByte: Int = headByte + (headByte and 0x1)

            return ((baseBytes[0] + kind.id) shl 16) or ((baseBytes[1] + headByte) shl 8) or (baseBytes[2] + tailByte)
        }

        fun decodeFromColor(base: Int, code: Int, config: BotConfig, messageRef: MessageRef): Option<PageNavigationState> {
            val bytes = base.toBytes()
                .zip(code.toBytes()) { a, b -> b - a }
                .drop(1)

            val kind = NavigationKind.values().find(bytes.first().toShort())
            val page = bytes[1] + bytes[2]

            return Option.cond(kind != NavigationKind.BOARD && page in kind.range) {
                PageNavigationState(messageRef, kind, page, LinuxTime.nowWithOffset(config.navigatorExpireOffset))
            }
        }

    }

}

data class BoardNavigationState(
    override val page: Int,
    val focusInfo: FocusSolver.FocusInfo,
    override val expireDate: LinuxTime,
): NavigationState {

    override val kind = NavigationKind.BOARD

}
