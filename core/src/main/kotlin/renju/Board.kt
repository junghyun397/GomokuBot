package renju

import renju.native.RustyRenjuC
import renju.native.RustyRenjuCApi
import renju.native.readNativeUtf8String
import renju.notation.*
import java.lang.foreign.MemorySegment
import java.lang.ref.Cleaner

enum class MoveError {
    Exist,
    Forbidden,
}

class Board private constructor (
    private val nativePointer: MemorySegment,
    private val describe: RustyRenjuC.BoardDescribe,
) {

    init {
        cleaner.register(this, NativeBoardCleaner(this.nativePointer))
    }

    private val patterns: RustyRenjuC.BoardPatterns by lazy {
        loadPatterns(this.nativePointer)
    }

    val playerColor: Color get() = Color.from(this.describe.player_color)!!

    val stones: Int get() = this.describe.field.count { it.isStone }

    val hashKey: HashKey get() = HashKey(describe.hash_key)

    fun pattern(pos: Pos, color: Color): Int {
        return when (color) {
            Color.BLACK -> patterns.blackPatterns[pos.idx]
            Color.WHITE -> patterns.whitePatterns[pos.idx]
        }
    }

    fun isPosEmpty(pos: Pos): Boolean =
        !this.describe.field[pos.idx].isStone

    fun isLegalMove(pos: Pos): Boolean {
        val item = this.describe.field[pos.idx]

        return !item.isStone && (playerColor != Color.BLACK || !item.isForbidden)
    }

    fun stoneKind(pos: Pos): Color? =
        this.describe.field[pos.idx]
            .takeIf { it.isStone }
            ?.let { Color.from(it.content) }

    fun forbiddenKind(pos: Pos): ForbiddenKind? =
        this.describe.field[pos.idx]
            .takeIf { it.isForbidden }
            ?.let { ForbiddenKind.from(it.content) }

    fun set(pos: Pos?): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_set(
            this.nativePointer,
            pos?.idx ?: RustyRenjuCApi.constants.posNone,
        )
            ?: return this

        return fromNativePointer(pointer)
    }

    fun unset(pos: Pos?): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_unset(
            this.nativePointer,
            pos?.idx ?: RustyRenjuCApi.constants.posNone,
        )
            ?: return this

        return fromNativePointer(pointer)
    }

    fun validateMove(pos: Pos): MoveError? {
        if (!this.isPosEmpty(pos)) {
            return MoveError.Exist
        }

        if (!this.isLegalMove(pos)) {
            return MoveError.Forbidden
        }

        return null
    }

    fun winner(): GameResult? {
        if (this.describe.winner.isSome) {
            return GameResult.Win(
                GameResult.WinCause.FIVE_IN_A_ROW,
                Color.from(this.describe.winner.color)!!
            )
        }

        return if (this.stones >= Pos.BOARD_SIZE) GameResult.Full
        else null
    }

    fun winningSequence(): List<Pos>? =
        this.describe.winner
            .takeIf { it.isSome }
            ?.sequence
            ?.map { raw -> Pos.fromIdx(raw) }

    internal fun nativeHandle(): MemorySegment = this.nativePointer

    private val RustyRenjuC.BoardExportItem.isStone: Boolean
        get() = this.kind == RustyRenjuCApi.constants.exportItemStone

    private val RustyRenjuC.BoardExportItem.isForbidden: Boolean
        get() = this.kind == RustyRenjuCApi.constants.exportItemForbidden

    override fun toString(): String {
        val stringPointer = RustyRenjuCApi.lib.rusty_renju_board_to_string(nativePointer)
            ?: throw IllegalStateException()

        return stringPointer.readNativeUtf8String(4096)
    }

    companion object {

        private val cleaner: Cleaner = Cleaner.create()

        fun emptyBoard(): Board {
            return fromNativePointer(RustyRenjuCApi.lib.rusty_renju_empty_board()
                ?: throw IllegalStateException())
        }

        fun fromHistory(history: History): Board {
            return fromNativePointer(RustyRenjuCApi.lib.rusty_renju_board_from_history(
                history.sequence.map { it?.idx ?: RustyRenjuCApi.constants.posNone }.toIntArray(),
                history.sequence.size.toLong()
            )
                ?: throw IllegalStateException())
        }

        private fun fromNativePointer(pointer: MemorySegment): Board {
            return try {
                Board(
                    nativePointer = pointer,
                    describe = loadDescribe(pointer),
                )
            } catch (e: Throwable) {
                RustyRenjuCApi.lib.rusty_renju_board_free(pointer)
                throw e
            }
        }

        private fun loadDescribe(pointer: MemorySegment): RustyRenjuC.BoardDescribe =
            RustyRenjuCApi.lib.rusty_renju_board_describe(pointer)
                ?: throw IllegalStateException()

        private fun loadPatterns(pointer: MemorySegment): RustyRenjuC.BoardPatterns =
            RustyRenjuCApi.lib.rusty_renju_board_patterns(pointer)
                ?: throw IllegalStateException()

    }

    private class NativeBoardCleaner(private val pointer: MemorySegment?) : Runnable {

        override fun run() {
            RustyRenjuCApi.lib.rusty_renju_board_free(pointer)
        }

    }

}
