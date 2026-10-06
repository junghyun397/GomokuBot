package renju.native

import renju.notation.ColorContainer
import renju.notation.Pos
import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*

internal object RustyRenju {

    private val symbols = NativeSymbols(NativeLibraryLoader.libraryLookup("rusty_renju_c"), "rusty_renju")

    val colorBlack = this.symbols.byte("color_black")
    val colorWhite = this.symbols.byte("color_white")
    val colorNone = this.symbols.byte("color_none")
    val forbiddenDoubleThree = this.symbols.byte("forbidden_kind_double_three")
    val forbiddenDoubleFour = this.symbols.byte("forbidden_kind_double_four")
    val forbiddenOverline = this.symbols.byte("forbidden_kind_overline")
    val posNone = this.symbols.int("pos_none")
    val exportItemStone = this.symbols.byte("board_export_item_stone")
    val exportItemForbidden = this.symbols.byte("board_export_item_forbidden")
    val closedFourMask = this.symbols.int("closed_four_mask")
    val openFourMask = this.symbols.int("open_four_mask")
    val openThreeMask = this.symbols.int("open_three_mask")
    val closeThreeMask = this.symbols.int("close_three_mask")
    val fiveMask = this.repeat4x(0b0000_0001)
    val potentialMask = this.repeat4x(0b0000_0110)

    private val ruleRenju = this.symbols.byte("rule_renju")
    private val boardSize = this.symbols.long("board_size")
    private val boardAlignment = this.symbols.long("board_align")
    private val patternLayout = MemoryLayout.sequenceLayout(256, JAVA_INT)
    private val patternsLayout = MemoryLayout.sequenceLayout(2, this.patternLayout)

    private val emptyBoardCall = this.symbols.function("empty_board", JAVA_BOOLEAN, JAVA_BYTE, ADDRESS)
    private val fromHistoryCall = this.symbols.function("board_from_history", JAVA_BOOLEAN, JAVA_BYTE, ADDRESS, JAVA_LONG, ADDRESS)
    private val toTextCall = this.symbols.function("board_to_string", JAVA_LONG, ADDRESS, ADDRESS, JAVA_LONG)
    private val setCall = this.symbols.function("board_set", JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS)
    private val unsetCall = this.symbols.function("board_unset", JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS)
    private val describeCall = this.symbols.function("board_describe", JAVA_BOOLEAN, ADDRESS, ADDRESS)
    private val patternsCall = this.symbols.function("board_pattens", JAVA_BOOLEAN, ADDRESS, ADDRESS)

    fun emptyBoard(): MemorySegment =
        checkNotNull(this.createBoard { out ->
            this.emptyBoardCall.invokeWithArguments(this.ruleRenju, out) as Boolean
        }) { "Native board initialization failed" }

    fun fromHistory(actions: IntArray?): MemorySegment =
        Arena.ofConfined().use { arena ->
            val input = actions.toNativeSegmentOrNull(arena)

            checkNotNull(this.createBoard { out ->
                this.fromHistoryCall.invokeWithArguments(this.ruleRenju, input, actions?.size?.toLong() ?: 0L, out) as Boolean
            }) { "Native board initialization from history failed" }
        }

    fun set(board: MemorySegment, pos: Pos?): MemorySegment? =
        this.createBoard { out ->
            this.setCall.invokeWithArguments(board, pos?.idx ?: this.posNone, out) as Boolean
        }

    fun unset(board: MemorySegment, pos: Pos?): MemorySegment? =
        this.createBoard { out ->
            this.unsetCall.invokeWithArguments(board, pos?.idx ?: this.posNone, out) as Boolean
        }

    fun describe(board: MemorySegment): BoardDescription =
        Arena.ofConfined().use { arena ->
            val out = arena.allocate(BoardDescription.layout)
            check(this.describeCall.invokeWithArguments(board, out) as Boolean) { "Native board description failed" }

            BoardDescription(out)
        }

    fun patterns(board: MemorySegment): ColorContainer<IntArray> =
        Arena.ofConfined().use { arena ->
            val out = arena.allocate(this.patternsLayout)
            check(this.patternsCall.invokeWithArguments(board, out) as Boolean) { "Native board pattern extraction failed" }

            val size = Pos.BOARD_SIZE * JAVA_INT.byteSize()

            ColorContainer(
                black = out.asSlice(0, size).toArray(JAVA_INT),
                white = out.asSlice(this.patternLayout.byteSize(), size).toArray(JAVA_INT),
            )
        }

    fun toText(board: MemorySegment): String {
        val size = this.toTextCall.invokeWithArguments(board, MemorySegment.NULL, 0L) as Long
        check(size > 0) { "Native board string conversion failed" }

        return Arena.ofConfined().use { arena ->
            val out = arena.allocate(size)
            this.toTextCall.invokeWithArguments(board, out, size)

            out.getString(0)
        }
    }

    private fun createBoard(initialize: (MemorySegment) -> Boolean): MemorySegment? =
        Arena.ofAuto().allocate(this.boardSize, this.boardAlignment).takeIf(initialize)

    private fun repeat4x(value: Int): Int =
        value or (value shl 8) or (value shl 16) or (value shl 24)

}
