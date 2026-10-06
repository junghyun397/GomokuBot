package core.session.entities

import arrow.core.Either
import core.BotConfig
import core.assets.*
import core.database.DatabaseConnection
import core.engine.FocusSolver
import core.interact.i18n.Language
import core.interact.message.HelpPages
import core.interact.message.SettingMapping
import renju.notation.Pos
import utils.Identifiable
import kotlin.time.Clock
import kotlin.time.Instant

enum class NavigationKind(override val id: Short, val range: Either<context(DatabaseConnection) () -> IntRange, IntRange>, val navigators: Set<String>) : Identifiable {

    BOARD(0, Either.Right(0 until Pos.BOARD_SIZE), setOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)),
    // 0: language setting
    SETTINGS(1, Either.Right(0 .. SettingMapping.map.size), setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    // 0: about gomokubot
    ABOUT(2, Either.Right(0 .. HelpPages.documents[Language.ENG.container]!!.first.size), setOf(UNICODE_LEFT, UNICODE_RIGHT)),
    ANNOUNCE(3, Either.Left { 1 .. contextOf<DatabaseConnection>().localCaches.announceCache.size }, setOf(UNICODE_LEFT, UNICODE_RIGHT));

    context(connection: DatabaseConnection)
    fun fetchRange(): IntRange =
        this.range.fold(
            ifLeft = { fetcher -> fetcher() },
            ifRight = { it }
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
    override val kind: NavigationKind,
    override val page: Int,
    override val expireDate: Instant,
) : NavigationState {

    companion object {

        fun encodeToColor(base: Int, kind: NavigationKind, page: Int): Int {
            require(page in 0..510)

            val headByte: Int = page shr 1
            val tailByte: Int = headByte + (page and 0x1)

            val red = ((base ushr 16) + kind.id) and 0xFF
            val green = ((base ushr 8) + headByte) and 0xFF
            val blue = (base + tailByte) and 0xFF

            return (red shl 16) or (green shl 8) or blue
        }

        context(connection: DatabaseConnection)
        fun decodeFromColor(base: Int, code: Int): PageNavigationState? {
            val kindRaw = ((code ushr 16) - (base ushr 16)) and 0xFF
            val pageTop = ((code ushr 8) - (base ushr 8)) and 0xFF
            val pageBottom = (code - base) and 0xFF

            val kind = NavigationKind.entries.find { it.id.toInt() == kindRaw }
                ?: return null
            val page = pageTop + pageBottom

            return if (kind != NavigationKind.BOARD && page in kind.fetchRange())
                PageNavigationState(kind, page, Clock.System.now() + BotConfig.navigatorExpireAfter)
            else null
        }

    }

}

data class BoardNavigationState(
    val initialFocus: FocusSolver.FocusInfo,
    val focus: Pos = initialFocus.focus,
)
