package renju

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import renju.native.RustyRenjuC
import renju.native.RustyRenjuCApi
import renju.notation.Color
import renju.notation.ForbiddenKind
import renju.notation.GameResult
import renju.notation.Pos
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.ref.Cleaner

enum class MoveError {
    Exist,
    Forbidden,
}

class Board private constructor (
    private val nativePointer: MemorySegment,
) {

    private val cleanable = cleaner.register(this, NativeBoardCleaner(nativePointer))

    val isNextColorBlack: Boolean
        get() = this.playerColor == Color.Black

    val playerColor: Color get() = Color.fromNative(RustyRenjuCApi.lib.rusty_renju_board_player_color(nativePointer))

    val opponentColor: Color get() = this.playerColor.reversed()

    val stones: Int get() = RustyRenjuCApi.lib.rusty_renju_board_stones(nativePointer).toUByte().toInt()

    fun pattern(pos: Pos, color: Color): Int = this.pattern(pos.idx, color)

    fun pattern(idx: Int, color: Color): Int {
        return RustyRenjuCApi.lib.rusty_renju_board_pattern(nativePointer, color.flag(), idx.toByte())
    }

    fun isPosEmpty(pos: Pos): Boolean = this.isPosEmpty(pos.idx)

    fun isPosEmpty(idx: Int): Boolean =
        RustyRenjuCApi.lib.rusty_renju_board_is_pos_empty(nativePointer, idx.toByte())

    fun isLegalMove(pos: Pos): Boolean = this.isLegalMove(pos.idx)

    fun isLegalMove(idx: Int): Boolean =
        RustyRenjuCApi.lib.rusty_renju_board_is_pos_empty(nativePointer, idx.toByte()) &&
        RustyRenjuCApi.lib.rusty_renju_board_is_legal_move(nativePointer, idx.toByte())

    fun stoneKind(pos: Pos): Color? =
        this.stoneKind(pos.idx)

    fun stoneKind(idx: Int): Color? =
        Color.fromFlag(RustyRenjuCApi.lib.rusty_renju_board_stone_kind(nativePointer, idx.toByte()))

    fun set(pos: Pos?): Board = this.setMaybe(pos?.idx)

    fun set(idx: Int): Board = this.setMaybe(idx)

    private fun setMaybe(idx: Int?): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_set(
            nativePointer,
            idx?.toByte() ?: RustyRenjuCApi.constants.posNone,
        )
            ?: return this

        return Board(pointer)
    }

    fun unset(pos: Pos?): Board = this.unsetMaybe(pos?.idx)

    fun unset(idx: Int): Board = this.unsetMaybe(idx)

    private fun unsetMaybe(idx: Int?): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_unset(
            nativePointer,
            idx?.toByte() ?: RustyRenjuCApi.constants.posNone,
        )
            ?: return this

        return Board(pointer)
    }

    fun validateMove(pos: Pos): Option<MoveError> =
        this.validateMove(pos.idx)

    fun validateMove(idx: Int): Option<MoveError> {
        if (!this.isPosEmpty(idx)) {
            return Some(MoveError.Exist)
        }

        if (!this.isLegalMove(idx)) {
            return Some(MoveError.Forbidden)
        }

        return None
    }

    val field: ByteArray by lazy { this.field(History.empty()) }

    fun field(history: History): ByteArray {
        val field = ByteArray(Pos.BOARD_SIZE) { Color.emptyFlag() }
        val actions = history.toMaybePosBuffer()

        val describePointer = RustyRenjuCApi.lib.rusty_renju_board_describe(
            nativePointer,
            actions,
            history.moves.toLong(),
        ) ?: return field

        try {
            val describe = RustyRenjuC.BoardDescribe(describePointer)

            for (idx in 0 until Pos.BOARD_SIZE) {
                val item = describe.field[idx]
                field[idx] = when (item.kind) {
                    RustyRenjuCApi.constants.exportItemStone ->
                        Color.fromFlag(item.stone.color)?.flag() ?: Color.emptyFlag()

                    RustyRenjuCApi.constants.exportItemForbidden ->
                        ForbiddenKind.fromNativeFlag(item.forbidden_kind)?.fieldFlag() ?: Color.emptyFlag()

                    else -> Color.emptyFlag()
                }
            }
        } finally {
            RustyRenjuCApi.lib.rusty_renju_board_describe_free(describePointer)
        }

        return field
    }

    fun winner(): Option<GameResult> {
        val fiveMask = RustyRenjuCApi.constants.fiveMask

        for (idx in 0 until Pos.BOARD_SIZE) {
            val color = this.stoneKind(idx) ?: continue

            if (this.pattern(idx, color) and fiveMask != 0) {
                return Some(GameResult.FiveInRow(color))
            }
        }

        return if (this.stones >= Pos.BOARD_SIZE) Some(GameResult.Full)
        else None
    }

    fun legalMoves(): IntArray {
        val moves = ArrayList<Int>()

        for (idx in 0 until Pos.BOARD_SIZE) {
            if (this.isLegalMove(idx)) {
                moves += idx
            }
        }

        return moves.toIntArray()
    }

    internal fun nativeHandle(): MemorySegment = nativePointer

    override fun toString(): String {
        val stringPointer = RustyRenjuCApi.lib.rusty_renju_board_to_string(nativePointer)
            ?: throw IllegalStateException()

        return readNativeUtf8String(stringPointer)
    }

    companion object {

        private val cleaner: Cleaner = Cleaner.create()

        fun newBoard(): Board {
            return Board(RustyRenjuCApi.lib.rusty_renju_default_board()
                ?: throw IllegalStateException())
        }

        fun fromHistory(history: History): Board {
            return Board(RustyRenjuCApi.lib.rusty_renju_board_from_history(
                history.toMaybePosBuffer(),
                history.moves.toLong(),
            ) ?: throw IllegalStateException())
        }

        private const val MAX_NATIVE_STRING_BYTES = 4_096L

        private fun readNativeUtf8String(pointer: MemorySegment): String {
            val cString = pointer.reinterpret(MAX_NATIVE_STRING_BYTES)
            var length = 0L

            while (length < MAX_NATIVE_STRING_BYTES && cString.get(ValueLayout.JAVA_BYTE, length) != 0.toByte()) {
                length++
            }

            val bytes = ByteArray(length.toInt())
            for (index in bytes.indices) {
                bytes[index] = cString.get(ValueLayout.JAVA_BYTE, index.toLong())
            }

            return bytes.toString(Charsets.UTF_8)
        }

    }

    private class NativeBoardCleaner(private val pointer: MemorySegment?) : Runnable {

        override fun run() {
            RustyRenjuCApi.lib.rusty_renju_board_free(pointer)
        }

    }

}
