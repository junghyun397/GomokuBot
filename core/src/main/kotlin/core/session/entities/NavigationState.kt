package core.session.entities

import core.BotConfig
import core.assets.*
import core.database.DatabaseConnection
import core.inference.FocusSolver
import core.interact.i18n.Language
import core.interact.message.MessagingServiceImpl
import core.interact.message.SettingMapping
import renju.notation.Renju
import utils.assets.LinuxTime
import utils.assets.toBytes
import utils.structs.*

enum class NavigationKind(override val id: Short, val range: Either<IntRange, (DatabaseConnection) -> IntRange>, val navigators: Set<String>) : Identifiable {

    BOARD(0, Either.Left(0 until Renju.BOARD_SIZE()), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    // 0: language setting
    SETTINGS(1, Either.Left(0 .. SettingMapping.map.size), setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    // 0: about gomokubot
    ABOUT(2, Either.Left(0 .. MessagingServiceImpl.aboutRenjuDocument[Language.ENG.container]!!.first.size), setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ANNOUNCE(3, Either.Right { connection -> 1 .. connection.localCaches.announceCache.size }, setOf(UNICODE_LEFT, UNICODE_RIGHT));

    fun fetchRange(dbConnection: DatabaseConnection): IntRange =
        this.range.fold(
            onLeft = { it },
            onRight = { fetcher -> fetcher(dbConnection) }
        )

    companion object {

        val navigators: Set<String> = entries
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

    companion object {

        fun encodeToColor(base: Int, kind: NavigationKind, page: Int): Int {
            val baseBytes = base.toBytes()
                .drop(1)
                .map { it.toUByte().toInt() }

            val headByte: Int = page shr 1
            val tailByte: Int = headByte + (headByte and 0x1)

            return ((baseBytes[0] + kind.id) shl 16) or ((baseBytes[1] + headByte) shl 8) or (baseBytes[2] + tailByte)
        }

        fun decodeFromColor(base: Int, code: Int, config: BotConfig, messageRef: MessageRef, dbConnection: DatabaseConnection): Option<PageNavigationState> {
            val (kindRaw, pageTop, pageBottom) = base.toBytes()
                .zip(code.toBytes()) { a, b -> b - a }
                .drop(1)

            val kind = NavigationKind.entries.find(kindRaw.toShort())
            val page = pageTop + pageBottom

            return Option.cond(kind != NavigationKind.BOARD && page in kind.fetchRange(dbConnection)) {
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
