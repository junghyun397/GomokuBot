package renju

import renju.native.RustyRenju
import renju.notation.*
import java.lang.foreign.MemorySegment

enum class MoveError {
    Exist,
    Forbidden,
}

class Board private constructor (
    private val nativePointer: MemorySegment,
) {

    private val description = RustyRenju.describe(this.nativePointer)

    private val patterns by lazy {
        RustyRenju.patterns(this.nativePointer)
    }

    val playerColor: Color get() = this.description.playerColor

    val stones: Int get() = this.description.cells.count { it.stone != null }

    val hashKey: HashKey get() = this.description.hashKey

    fun pattern(pos: Pos, color: Color): Int =
        this.patterns[color][pos.idx]

    fun isPosEmpty(pos: Pos): Boolean =
        this.stoneKind(pos) == null

    fun isLegalMove(pos: Pos): Boolean {
        val cell = this.description.cells[pos.idx]

        return cell.stone == null && (this.playerColor != Color.BLACK || cell.forbidden == null)
    }

    fun stoneKind(pos: Pos): Color? =
        this.description.cells[pos.idx].stone

    fun forbiddenKind(pos: Pos): ForbiddenKind? =
        this.description.cells[pos.idx].forbidden

    fun set(pos: Pos?): Board {
        val pointer = RustyRenju.set(this.nativePointer, pos) ?: return this

        return Board(pointer)
    }

    fun unset(pos: Pos?): Board {
        val pointer = RustyRenju.unset(this.nativePointer, pos) ?: return this

        return Board(pointer)
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
        val winner = this.description.winner

        if (winner != null) {
            return GameResult.Win(GameResult.WinCause.FIVE_IN_A_ROW, winner.color)
        }

        return if (this.stones >= Pos.BOARD_SIZE) GameResult.Full
        else null
    }

    fun winningSequence(): List<Pos>? =
        this.description.winner?.sequence?.toList()

    internal fun nativeHandle(): MemorySegment = this.nativePointer

    override fun toString(): String =
        RustyRenju.toText(this.nativePointer)

    companion object {

        fun emptyBoard(): Board =
            Board(RustyRenju.emptyBoard())

        fun fromHistory(history: History): Board =
            Board(RustyRenju.fromHistory(history.toMaybePosBuffer()))

    }

}
