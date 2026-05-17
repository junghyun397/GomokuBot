package renju.native

import renju.notation.Pos
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement

internal class RustyRenjuC internal constructor(
    lookup: SymbolLookup,
) {

    private val symbols = NativeSymbols(lookup, "rusty_renju")

    val constants = Constants(
        colorBlack = symbols.byte("color_black"),
        colorWhite = symbols.byte("color_white"),
        colorNone = symbols.byte("color_none"),
        forbiddenNone = symbols.byte("forbidden_kind_none"),
        forbiddenDoubleThree = symbols.byte("forbidden_kind_double_three"),
        forbiddenDoubleFour = symbols.byte("forbidden_kind_double_four"),
        forbiddenOverline = symbols.byte("forbidden_kind_double_overline"),
        posNone = symbols.byte("pos_none"),
        exportItemEmpty = symbols.byte("board_export_item_empty"),
        exportItemStone = symbols.byte("board_export_item_stone"),
        exportItemForbidden = symbols.byte("board_export_item_forbidden"),
        closedFourMask = symbols.int("closed_four_mask"),
        openFourMask = symbols.int("open_four_mask"),
        fiveMask = symbols.int("five_mask"),
        openThreeMask = symbols.int("open_three_mask"),
        closeThreeMask = symbols.int("close_three_mask"),
        potentialMask = symbols.int("potential_mask"),
        emptyHash = symbols.long("empty_hash")
    )

    private val defaultBoard = symbols.function("default_board", ValueLayout.ADDRESS)
    private val boardFromHistory = symbols.function("board_from_history", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    private val boardFromString = symbols.function("board_from_string", ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardToString = symbols.function("board_to_string", ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardSet = symbols.function("board_set", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardUnset = symbols.function("board_unset", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardFree = symbols.voidFunction("board_free", ValueLayout.ADDRESS)
    private val boardDescribeInto = symbols.function("board_describe_into", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardPattensInto = symbols.function("board_pattens_into", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS)

    fun rusty_renju_default_board(): MemorySegment? =
        (defaultBoard.invokeWithArguments() as MemorySegment).nullIfNull()

    fun rusty_renju_board_from_history(actions: ByteArray?, len: Long): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            (boardFromHistory.invokeWithArguments(actions.toNativeSegmentOrNull(arena), len) as MemorySegment)
                .nullIfNull()
        }
    }

    fun rusty_renju_board_from_string(source: String?): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            val sourceSegment = source?.let(arena::allocateFrom) ?: MemorySegment.NULL
            (boardFromString.invokeWithArguments(sourceSegment) as MemorySegment).nullIfNull()
        }
    }

    fun rusty_renju_board_to_string(board: MemorySegment?): MemorySegment? =
        (boardToString.invokeWithArguments(board.orNullAddress()) as MemorySegment).nullIfNull()

    fun rusty_renju_board_set(board: MemorySegment?, pos: Byte): MemorySegment? =
        (boardSet.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_unset(board: MemorySegment?, pos: Byte): MemorySegment? =
        (boardUnset.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_free(board: MemorySegment?) {
        boardFree.invokeWithArguments(board.orNullAddress())
    }

    fun rusty_renju_board_describe(board: MemorySegment?): BoardDescribe? {
        return Arena.ofConfined().use { arena ->
            val describe = arena.allocate(BOARD_DESCRIBE_NATIVE_SIZE, BOARD_DESCRIBE_NATIVE_ALIGNMENT)
            val success = boardDescribeInto.invokeWithArguments(
                board.orNullAddress(),
                describe,
            ) as Boolean

            if (success) BoardDescribe(describe) else null
        }
    }

    fun rusty_renju_board_patterns(board: MemorySegment?): BoardPatterns? {
        return Arena.ofConfined().use { arena ->
            val patterns = arena.allocate(BOARD_PATTERNS_LAYOUT)
            val success = boardPattensInto.invokeWithArguments(
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
        val forbiddenNone: Byte,
        val forbiddenDoubleThree: Byte,
        val forbiddenDoubleFour: Byte,
        val forbiddenOverline: Byte,
        val posNone: Byte,
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

    data class BoardExportItem(val kind: Byte, val stone: Byte, val forbidden_kind: Byte)

    data class BoardWinner(val isSome: Boolean, val color: Byte, val sequence: ByteArray)

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
                val stone = describeSegment.get(ValueLayout.JAVA_BYTE, itemOffset + BOARD_EXPORT_ITEM_STONE_OFFSET)
                val forbiddenKind = describeSegment.get(
                    ValueLayout.JAVA_BYTE,
                    itemOffset + BOARD_EXPORT_ITEM_FORBIDDEN_KIND_OFFSET,
                )

                BoardExportItem(
                    kind = kind,
                    stone = stone,
                    forbidden_kind = forbiddenKind,
                )
            }
            winner = BoardWinner(
                isSome = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_IS_SOME_OFFSET) != 0.toByte(),
                color = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_COLOR_OFFSET),
                sequence = ByteArray(BOARD_WINNER_SEQUENCE_SIZE) { idx ->
                    describeSegment.get(
                        ValueLayout.JAVA_BYTE,
                        BOARD_DESCRIBE_WINNER_OFFSET + BOARD_WINNER_SEQUENCE_OFFSET + idx.toLong() * ValueLayout.JAVA_BYTE.byteSize(),
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

        private val BOARD_EXPORT_ITEM_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("kind"),
            ValueLayout.JAVA_BYTE.withName("stone"),
            ValueLayout.JAVA_BYTE.withName("forbidden_kind"),
        )
        private val BOARD_WINNER_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("is_some"),
            ValueLayout.JAVA_BYTE.withName("color"),
            MemoryLayout.sequenceLayout(BOARD_WINNER_SEQUENCE_SIZE.toLong(), ValueLayout.JAVA_BYTE).withName("sequence"),
        )
        private val BOARD_DESCRIBE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("hash_key"),
            ValueLayout.JAVA_BYTE.withName("player_color"),
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), BOARD_EXPORT_ITEM_LAYOUT).withName("field"),
            BOARD_WINNER_LAYOUT.withName("winner"),
        )
        private val BOARD_PATTERNS_LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), ValueLayout.JAVA_INT).withName("black_pattens"),
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), ValueLayout.JAVA_INT).withName("white_pattens"),
        )

        private val BOARD_DESCRIBE_NATIVE_ALIGNMENT = ValueLayout.JAVA_LONG.byteAlignment()
        private val BOARD_DESCRIBE_NATIVE_SIZE = align(BOARD_DESCRIBE_LAYOUT.byteSize(), BOARD_DESCRIBE_NATIVE_ALIGNMENT)

        private val BOARD_EXPORT_ITEM_SIZE = BOARD_EXPORT_ITEM_LAYOUT.byteSize()
        private val BOARD_EXPORT_ITEM_KIND_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("kind"))
        private val BOARD_EXPORT_ITEM_STONE_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("stone"))
        private val BOARD_EXPORT_ITEM_FORBIDDEN_KIND_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("forbidden_kind"))
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

    }

}

internal object RustyRenjuCApi {

    val lib: RustyRenjuC by lazy { RustyRenjuC(NativeLibraryLoader.libraryLookup("rusty_renju_c")) }

    val constants: RustyRenjuC.Constants get() = lib.constants

}
