package renju

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.sun.jna.Pointer
import renju.native.RustyRenjuC
import renju.native.RustyRenjuCApi
import renju.notation.Color
import renju.notation.ForbiddenKind
import renju.notation.GameResult
import renju.notation.Pos
import java.lang.ref.Cleaner

enum class MoveError {
    Exist,
    Forbidden,
}

class Board private constructor (
    private val nativePointer: Pointer,
    private val lastMoveIndex: Int,
) {

    private val cleanable = cleaner.register(this, NativeBoardCleaner(nativePointer))

    val isNextColorBlack: Boolean
        get() = this.playerColor() == Color.Black

    fun playerColor(): Color = Color.fromNative(RustyRenjuCApi.lib.rusty_renju_board_player_color(nativePointer))

    fun color(): Color = this.playerColor().reversed()

    fun stones(): Int = RustyRenjuCApi.lib.rusty_renju_board_stones(nativePointer).toUByte().toInt()

    fun moves(): Int = this.stones()

    fun pattern(pos: Pos, color: Color): Int = this.pattern(pos.idx(), color)

    fun pattern(posIndex: Int, color: Color): Int {
        return RustyRenjuCApi.lib.rusty_renju_board_pattern(nativePointer, color.flag(), posIndex.toByte())
    }

    fun isPosEmpty(pos: Pos): Boolean = this.isPosEmpty(pos.idx())

    fun isPosEmpty(posIndex: Int): Boolean =
        RustyRenjuCApi.lib.rusty_renju_board_is_pos_empty(nativePointer, posIndex.toByte())

    fun isLegalMove(pos: Pos): Boolean = this.isLegalMove(pos.idx())

    fun isLegalMove(posIndex: Int): Boolean =
        RustyRenjuCApi.lib.rusty_renju_board_is_pos_empty(nativePointer, posIndex.toByte()) &&
        RustyRenjuCApi.lib.rusty_renju_board_is_legal_move(nativePointer, posIndex.toByte())

    fun stoneKind(pos: Pos): Color? =
        this.stoneKind(pos.idx())

    fun stoneKind(posIndex: Int): Color? =
        Color.fromFlag(RustyRenjuCApi.lib.rusty_renju_board_stone_kind(nativePointer, posIndex.toByte()))

    fun lastMove(): Int = this.lastMoveIndex

    fun lastPos(): Option<Pos> =
        if (Pos.isValidIdx(this.lastMoveIndex)) {
            Some(Pos.fromIdx(this.lastMoveIndex))
        } else {
            None
        }

    fun set(pos: Pos): Board = this.set(pos.idx())

    fun set(posIndex: Int): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_set(nativePointer, posIndex.toByte())
            ?: return this

        return Board(pointer, posIndex)
    }

    fun unset(pos: Pos): Board = this.unset(pos.idx())

    fun unset(posIndex: Int): Board {
        val pointer = RustyRenjuCApi.lib.rusty_renju_board_unset(nativePointer, posIndex.toByte())
            ?: return this

        return Board(pointer, -1)
    }

    fun validateMove(pos: Pos): Option<MoveError> =
        this.validateMove(pos.idx())

    fun validateMove(posIndex: Int): Option<MoveError> {
        if (!this.isPosEmpty(posIndex)) {
            return Some(MoveError.Exist)
        }

        if (!this.isLegalMove(posIndex)) {
            return Some(MoveError.Forbidden)
        }

        return None
    }

    val field: ByteArray by lazy {
        val field = ByteArray(Pos.BOARD_SIZE) { Color.emptyFlag() }

        val describePointer = RustyRenjuCApi.lib.rusty_renju_board_describe(nativePointer, null, 0)
            ?: return@lazy field

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

        field
    }

    fun winner(): Option<GameResult> {
        val fiveMask = RustyRenjuCApi.constants.fiveMask

        for (idx in 0 until Pos.BOARD_SIZE) {
            val color = this.stoneKind(idx) ?: continue

            if (this.pattern(idx, color) and fiveMask != 0) {
                return Some(GameResult.FiveInRow(color))
            }
        }

        return if (this.stones() >= Pos.BOARD_SIZE) Some(GameResult.Full)
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

    internal fun nativeHandle(): Pointer = nativePointer

    override fun toString(): String =
        RustyRenjuCApi.lib.rusty_renju_board_to_string(nativePointer)
            ?.getString(0)
            ?: throw IllegalStateException()

    companion object {

        private val cleaner: Cleaner = Cleaner.create()

        fun newBoard(): Board {
            return Board(RustyRenjuCApi.lib.rusty_renju_default_board()
                ?: throw IllegalStateException(), -1)
        }

        fun fromBoardText(source: String?, lastMove: Int): Option<Board> {
            val text = source?.takeIf { it.isNotBlank() } ?: return None

            val pointer = RustyRenjuCApi.lib.rusty_renju_board_from_string(text)
                ?: return None

            return Some(fromNative(pointer, normalizeLastMove(lastMove)))
        }

        fun fromFieldArray(field: ByteArray, lastMove: Int): Option<Board> {
            val blackMoves = ArrayList<Int>()
            val whiteMoves = ArrayList<Int>()

            for (idx in 0 until Pos.BOARD_SIZE) {
                when (Color.fromFlag(field.getOrElse(idx) { Color.emptyFlag() })) {
                    Color.Black -> blackMoves += idx
                    Color.White -> whiteMoves += idx
                    null -> Unit
                }
            }

            if (whiteMoves.size > blackMoves.size || blackMoves.size - whiteMoves.size > 1) {
                return None
            }

            val history = ByteArray(blackMoves.size + whiteMoves.size)
            var blackIndex = 0
            var whiteIndex = 0
            var historyIndex = 0

            while (blackIndex < blackMoves.size || whiteIndex < whiteMoves.size) {
                if (blackIndex >= blackMoves.size) {
                    return None
                }

                history[historyIndex++] = blackMoves[blackIndex++].toByte()

                if (whiteIndex < whiteMoves.size) {
                    history[historyIndex++] = whiteMoves[whiteIndex++].toByte()
                }
            }

            val pointer = RustyRenjuCApi.lib.rusty_renju_board_from_history(history, history.size.toLong())
                ?: return None

            return Some(fromNative(pointer, normalizeLastMove(lastMove)))
        }

        private fun normalizeLastMove(lastMove: Int): Int =
            if (Pos.isValidIdx(lastMove)) lastMove
            else -1

        internal fun fromNative(pointer: Pointer, lastMoveIndex: Int = -1): Board =
            Board(pointer, lastMoveIndex)

    }

    private class NativeBoardCleaner(private val pointer: Pointer?) : Runnable {

        override fun run() {
            RustyRenjuCApi.lib.rusty_renju_board_free(pointer)
        }

    }

}
