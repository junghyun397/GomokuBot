package renju.native

import renju.notation.Pos
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement

internal class RustyRenjuC internal constructor(
    lookup: SymbolLookup,
) {

    private val symbols = NativeSymbols(lookup, "rusty_renju")

    val constants = Constants(
        colorBlack = this.symbols.byte("color_black"),
        colorWhite = this.symbols.byte("color_white"),
        colorNone = this.symbols.byte("color_none"),
        ruleRenju = this.symbols.byte("rule_renju"),
        forbiddenNone = this.symbols.byte("forbidden_kind_none"),
        forbiddenDoubleThree = this.symbols.byte("forbidden_kind_double_three"),
        forbiddenDoubleFour = this.symbols.byte("forbidden_kind_double_four"),
        forbiddenOverline = this.symbols.byte("forbidden_kind_overline"),
        posNone = this.symbols.int("pos_none"),
        exportItemEmpty = this.symbols.byte("board_export_item_empty"),
        exportItemStone = this.symbols.byte("board_export_item_stone"),
        exportItemForbidden = this.symbols.byte("board_export_item_forbidden"),
        closedFourMask = this.symbols.int("closed_four_mask"),
        openFourMask = this.symbols.int("open_four_mask"),
        fiveMask = repeat4x(0b0000_0001),
        openThreeMask = this.symbols.int("open_three_mask"),
        closeThreeMask = this.symbols.int("close_three_mask"),
        potentialMask = repeat4x(0b0000_0110),
        emptyHash = this.symbols.long("empty_hash")
    )

    private val emptyBoard = this.symbols.function("empty_board", ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardFromHistory = this.symbols.function("board_from_history", ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    private val boardFromString = this.symbols.function("board_from_string", ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS)
    private val boardToString = this.symbols.function("board_to_string", ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardSet = this.symbols.function("board_set", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    private val boardUnset = this.symbols.function("board_unset", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    private val boardFree = this.symbols.voidFunction("board_free", ValueLayout.ADDRESS)
    private val boardDescribe = this.symbols.function("board_describe", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardPattens = this.symbols.function("board_pattens", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS)

    fun rusty_renju_empty_board(ruleKind: Byte = this.constants.ruleRenju): MemorySegment? =
        (this.emptyBoard.invokeWithArguments(ruleKind) as MemorySegment).nullIfNull()

    fun rusty_renju_board_from_history(actions: IntArray?, len: Long, ruleKind: Byte = this.constants.ruleRenju): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            (this.boardFromHistory.invokeWithArguments(ruleKind, actions.toNativeSegmentOrNull(arena), len) as MemorySegment)
                .nullIfNull()
        }
    }

    fun rusty_renju_board_from_string(source: String?, ruleKind: Byte = this.constants.ruleRenju): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            val sourceSegment = source?.let(arena::allocateFrom) ?: MemorySegment.NULL
            (this.boardFromString.invokeWithArguments(ruleKind, sourceSegment) as MemorySegment).nullIfNull()
        }
    }

    fun rusty_renju_board_to_string(board: MemorySegment?): MemorySegment? =
        (this.boardToString.invokeWithArguments(board.orNullAddress()) as MemorySegment).nullIfNull()

    fun rusty_renju_board_set(board: MemorySegment?, pos: Int): MemorySegment? =
        (this.boardSet.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_unset(board: MemorySegment?, pos: Int): MemorySegment? =
        (this.boardUnset.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_free(board: MemorySegment?) {
        this.boardFree.invokeWithArguments(board.orNullAddress())
    }

    fun rusty_renju_board_describe(board: MemorySegment?): BoardDescribe? {
        return Arena.ofConfined().use { arena ->
            val describe = arena.allocate(BOARD_DESCRIBE_NATIVE_SIZE, BOARD_DESCRIBE_NATIVE_ALIGNMENT)
            val success = this.boardDescribe.invokeWithArguments(
                board.orNullAddress(),
                describe,
            ) as Boolean

            if (success) BoardDescribe(describe) else null
        }
    }

    fun rusty_renju_board_patterns(board: MemorySegment?): BoardPatterns? {
        return Arena.ofConfined().use { arena ->
            val patterns = arena.allocate(BOARD_PATTERNS_LAYOUT)
            val success = this.boardPattens.invokeWithArguments(
                board.orNullAddress(),
                patterns,
            ) as Boolean

            if (success) BoardPatterns(patterns) else null
        }
    }

    data class Constants(
        val colorBlack: Byte,
        val colorWhite: Byte,
        val colorNone: Byte,
        val ruleRenju: Byte,
        val forbiddenNone: Byte,
        val forbiddenDoubleThree: Byte,
        val forbiddenDoubleFour: Byte,
        val forbiddenOverline: Byte,
        val posNone: Int,
        val exportItemEmpty: Byte,
        val exportItemStone: Byte,
        val exportItemForbidden: Byte,
        val closedFourMask: Int,
        val openFourMask: Int,
        val fiveMask: Int,
        val openThreeMask: Int,
        val closeThreeMask: Int,
        val potentialMask: Int,
        val emptyHash: Long,
    )

    data class BoardExportItem(val kind: Byte, val content: Byte)

    data class BoardWinner(val isSome: Boolean, val color: Byte, val sequence: IntArray)

    class BoardDescribe(pointer: MemorySegment) {

        val hash_key: Long
        val player_color: Byte
        val field: Array<BoardExportItem>
        val winner: BoardWinner

        init {
            val describeSegment = pointer.reinterpret(BOARD_DESCRIBE_NATIVE_SIZE)

            hash_key = describeSegment.get(ValueLayout.JAVA_LONG, BOARD_DESCRIBE_HASH_KEY_OFFSET)
            player_color = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_PLAYER_COLOR_OFFSET)
            field = Array(Pos.BOARD_SIZE) { idx ->
                val itemOffset = BOARD_DESCRIBE_FIELD_OFFSET + idx.toLong() * BOARD_EXPORT_ITEM_SIZE
                val kind = describeSegment.get(ValueLayout.JAVA_BYTE, itemOffset + BOARD_EXPORT_ITEM_KIND_OFFSET)
                val content = describeSegment.get(ValueLayout.JAVA_BYTE, itemOffset + BOARD_EXPORT_ITEM_CONTENT_OFFSET)

                BoardExportItem(
                    kind = kind,
                    content = content,
                )
            }
            winner = BoardWinner(
                isSome = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_IS_SOME_OFFSET) != 0.toByte(),
                color = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_COLOR_OFFSET),
                sequence = IntArray(BOARD_WINNER_SEQUENCE_SIZE) { idx ->
                    describeSegment.get(
                        ValueLayout.JAVA_INT,
                        BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_SEQUENCE_OFFSET + idx.toLong() * ValueLayout.JAVA_INT.byteSize(),
                    )
                },
            )
        }

    }

    class BoardPatterns(pointer: MemorySegment) {

        val blackPatterns: IntArray
        val whitePatterns: IntArray

        init {
            val patternsSegment = pointer.reinterpret(BOARD_PATTERNS_LAYOUT.byteSize())

            blackPatterns = IntArray(Pos.BOARD_SIZE) { idx ->
                patternsSegment.get(
                    ValueLayout.JAVA_INT,
                    BOARD_PATTERNS_BLACK_OFFSET + idx.toLong() * ValueLayout.JAVA_INT.byteSize(),
                )
            }
            whitePatterns = IntArray(Pos.BOARD_SIZE) { idx ->
                patternsSegment.get(
                    ValueLayout.JAVA_INT,
                    BOARD_PATTERNS_WHITE_OFFSET + idx.toLong() * ValueLayout.JAVA_INT.byteSize(),
                )
            }
        }

    }

    private companion object {

        private const val BOARD_WINNER_SEQUENCE_SIZE = 5
        private const val BOARD_PATTERN_SIZE = 256

        private val BOARD_EXPORT_ITEM_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("kind"),
            ValueLayout.JAVA_BYTE.withName("content"),
        )
        private val BOARD_WINNER_SEQUENCE_PADDING_SIZE = this.align(
            ValueLayout.JAVA_BYTE.byteSize() + ValueLayout.JAVA_BYTE.byteSize(),
            ValueLayout.JAVA_INT.byteAlignment(),
        ) - ValueLayout.JAVA_BYTE.byteSize() - ValueLayout.JAVA_BYTE.byteSize()
        private val BOARD_WINNER_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("is_some"),
            ValueLayout.JAVA_BYTE.withName("color"),
            MemoryLayout.paddingLayout(BOARD_WINNER_SEQUENCE_PADDING_SIZE),
            MemoryLayout.sequenceLayout(BOARD_WINNER_SEQUENCE_SIZE.toLong(), ValueLayout.JAVA_INT).withName("sequence"),
        )
        private val BOARD_DESCRIBE_WINNER_PADDING_SIZE = this.align(
            ValueLayout.JAVA_LONG.byteSize() + ValueLayout.JAVA_BYTE.byteSize() + Pos.BOARD_SIZE * this.BOARD_EXPORT_ITEM_LAYOUT.byteSize(),
            this.BOARD_WINNER_LAYOUT.byteAlignment(),
        ) - ValueLayout.JAVA_LONG.byteSize() - ValueLayout.JAVA_BYTE.byteSize() - Pos.BOARD_SIZE * this.BOARD_EXPORT_ITEM_LAYOUT.byteSize()
        private val BOARD_DESCRIBE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("hash_key"),
            ValueLayout.JAVA_BYTE.withName("player_color"),
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), BOARD_EXPORT_ITEM_LAYOUT).withName("field"),
            MemoryLayout.paddingLayout(BOARD_DESCRIBE_WINNER_PADDING_SIZE),
            BOARD_WINNER_LAYOUT.withName("winner"),
        )
        private val BOARD_PATTERNS_LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.sequenceLayout(BOARD_PATTERN_SIZE.toLong(), ValueLayout.JAVA_INT).withName("black_pattens"),
            MemoryLayout.sequenceLayout(BOARD_PATTERN_SIZE.toLong(), ValueLayout.JAVA_INT).withName("white_pattens"),
        )

        private val BOARD_DESCRIBE_NATIVE_ALIGNMENT = ValueLayout.JAVA_LONG.byteAlignment()
        private val BOARD_DESCRIBE_NATIVE_SIZE = align(BOARD_DESCRIBE_LAYOUT.byteSize(), BOARD_DESCRIBE_NATIVE_ALIGNMENT)

        private val BOARD_EXPORT_ITEM_SIZE = BOARD_EXPORT_ITEM_LAYOUT.byteSize()
        private val BOARD_EXPORT_ITEM_KIND_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("kind"))
        private val BOARD_EXPORT_ITEM_CONTENT_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("content"))
        private val BOARD_WINNER_IS_SOME_OFFSET = BOARD_WINNER_LAYOUT.byteOffset(groupElement("is_some"))
        private val BOARD_WINNER_COLOR_OFFSET = BOARD_WINNER_LAYOUT.byteOffset(groupElement("color"))
        private val BOARD_WINNER_SEQUENCE_OFFSET = BOARD_WINNER_LAYOUT.byteOffset(groupElement("sequence"))

        private val BOARD_DESCRIBE_HASH_KEY_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("hash_key"))
        private val BOARD_DESCRIBE_PLAYER_COLOR_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("player_color"))
        private val BOARD_DESCRIBE_FIELD_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("field"))
        private val BOARD_DESCRIBE_WINNER_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("winner"))
        private val BOARD_PATTERNS_BLACK_OFFSET = BOARD_PATTERNS_LAYOUT.byteOffset(groupElement("black_pattens"))
        private val BOARD_PATTERNS_WHITE_OFFSET = BOARD_PATTERNS_LAYOUT.byteOffset(groupElement("white_pattens"))

        private fun align(size: Long, alignment: Long): Long =
            size + (alignment - size % alignment) % alignment

        private fun repeat4x(value: Int): Int =
            value or (value shl 8) or (value shl 16) or (value shl 24)

    }

}

internal object RustyRenjuCApi {

    val lib: RustyRenjuC by lazy { RustyRenjuC(NativeLibraryLoader.libraryLookup("rusty_renju_c")) }

    val constants: RustyRenjuC.Constants get() = lib.constants

}
