package renju.native

import renju.notation.Pos
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement

internal class RustyRenjuC internal constructor(
    lookup: SymbolLookup,
) {

    private val symbols = NativeSymbols(lookup, "rusty_renju")

    val constants: Constants = Constants(
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
    )

    private val defaultBoard = symbols.function("default_board", ValueLayout.ADDRESS)
    private val boardFromHistory = symbols.function("board_from_history", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    private val boardFromString = symbols.function("board_from_string", ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardToString = symbols.function("board_to_string", ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val boardPlayerColor = symbols.function("board_player_color", ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS)
    private val boardStones = symbols.function("board_stones", ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS)
    private val boardPattern = symbols.function("board_pattern", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE)
    private val boardIsPosEmpty = symbols.function("board_is_pos_empty", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardIsLegalMove = symbols.function("board_is_legal_move", ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardStoneKind = symbols.function("board_stone_kind", ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardSet = symbols.function("board_set", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardUnset = symbols.function("board_unset", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    private val boardFree = symbols.voidFunction("board_free", ValueLayout.ADDRESS)
    private val boardDescribe = symbols.function("board_describe", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    private val boardDescribeFree = symbols.voidFunction("board_describe_free", ValueLayout.ADDRESS)

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

    fun rusty_renju_board_player_color(board: MemorySegment?): Byte =
        boardPlayerColor.invokeWithArguments(board.orNullAddress()) as Byte

    fun rusty_renju_board_stones(board: MemorySegment?): Byte =
        boardStones.invokeWithArguments(board.orNullAddress()) as Byte

    fun rusty_renju_board_pattern(board: MemorySegment?, color: Byte, pos: Byte): Int =
        boardPattern.invokeWithArguments(board.orNullAddress(), color, pos) as Int

    fun rusty_renju_board_is_pos_empty(board: MemorySegment?, pos: Byte): Boolean =
        boardIsPosEmpty.invokeWithArguments(board.orNullAddress(), pos) as Boolean

    fun rusty_renju_board_is_legal_move(board: MemorySegment?, pos: Byte): Boolean =
        boardIsLegalMove.invokeWithArguments(board.orNullAddress(), pos) as Boolean

    fun rusty_renju_board_stone_kind(board: MemorySegment?, pos: Byte): Byte =
        boardStoneKind.invokeWithArguments(board.orNullAddress(), pos) as Byte

    fun rusty_renju_board_set(board: MemorySegment?, pos: Byte): MemorySegment? =
        (boardSet.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_unset(board: MemorySegment?, pos: Byte): MemorySegment? =
        (boardUnset.invokeWithArguments(board.orNullAddress(), pos) as MemorySegment).nullIfNull()

    fun rusty_renju_board_free(board: MemorySegment?) {
        boardFree.invokeWithArguments(board.orNullAddress())
    }

    fun rusty_renju_board_describe(board: MemorySegment?, maybePosSlice: ByteArray?, len: Long): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            (boardDescribe.invokeWithArguments(
                board.orNullAddress(),
                maybePosSlice.toNativeSegmentOrNull(arena),
                len,
            ) as MemorySegment).nullIfNull()
        }
    }

    fun rusty_renju_board_describe_free(describe: MemorySegment?) {
        boardDescribeFree.invokeWithArguments(describe.orNullAddress())
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
    )

    data class BoardExportStone(val color: Byte, val sequence: Byte)

    data class BoardExportItem(val kind: Byte, val stone: BoardExportStone, val forbidden_kind: Byte)

    class BoardDescribe(pointer: MemorySegment) {

        val hash_key: Long
        val player_color: Byte
        val field: Array<BoardExportItem>

        init {
            val describeSegment = pointer.reinterpret(BOARD_DESCRIBE_LAYOUT.byteSize())

            hash_key = describeSegment.get(ValueLayout.JAVA_LONG, BOARD_DESCRIBE_HASH_KEY_OFFSET)
            player_color = describeSegment.get(ValueLayout.JAVA_BYTE, BOARD_DESCRIBE_PLAYER_COLOR_OFFSET)
            field = Array(Pos.BOARD_SIZE) { idx ->
                val itemOffset = BOARD_DESCRIBE_FIELD_OFFSET + idx.toLong() * BOARD_EXPORT_ITEM_SIZE
                val kind = describeSegment.get(ValueLayout.JAVA_BYTE, itemOffset + BOARD_EXPORT_ITEM_KIND_OFFSET)
                val stoneColor = describeSegment.get(
                    ValueLayout.JAVA_BYTE,
                    itemOffset + BOARD_EXPORT_ITEM_STONE_OFFSET + BOARD_EXPORT_STONE_COLOR_OFFSET,
                )
                val stoneSequence = describeSegment.get(
                    ValueLayout.JAVA_BYTE,
                    itemOffset + BOARD_EXPORT_ITEM_STONE_OFFSET + BOARD_EXPORT_STONE_SEQUENCE_OFFSET,
                )
                val forbiddenKind = describeSegment.get(
                    ValueLayout.JAVA_BYTE,
                    itemOffset + BOARD_EXPORT_ITEM_FORBIDDEN_KIND_OFFSET,
                )

                BoardExportItem(
                    kind = kind,
                    stone = BoardExportStone(stoneColor, stoneSequence),
                    forbidden_kind = forbiddenKind,
                )
            }
        }

    }

    private companion object {

        private val BOARD_EXPORT_STONE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("color"),
            ValueLayout.JAVA_BYTE.withName("sequence"),
        )
        private val BOARD_EXPORT_ITEM_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_BYTE.withName("kind"),
            BOARD_EXPORT_STONE_LAYOUT.withName("stone"),
            ValueLayout.JAVA_BYTE.withName("forbidden_kind"),
        )
        private val BOARD_DESCRIBE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("hash_key"),
            ValueLayout.JAVA_BYTE.withName("player_color"),
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), BOARD_EXPORT_ITEM_LAYOUT).withName("field"),
        )

        private val BOARD_EXPORT_STONE_COLOR_OFFSET = BOARD_EXPORT_STONE_LAYOUT.byteOffset(groupElement("color"))
        private val BOARD_EXPORT_STONE_SEQUENCE_OFFSET = BOARD_EXPORT_STONE_LAYOUT.byteOffset(groupElement("sequence"))

        private val BOARD_EXPORT_ITEM_SIZE = BOARD_EXPORT_ITEM_LAYOUT.byteSize()
        private val BOARD_EXPORT_ITEM_KIND_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("kind"))
        private val BOARD_EXPORT_ITEM_STONE_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("stone"))
        private val BOARD_EXPORT_ITEM_FORBIDDEN_KIND_OFFSET = BOARD_EXPORT_ITEM_LAYOUT.byteOffset(groupElement("forbidden_kind"))

        private val BOARD_DESCRIBE_HASH_KEY_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("hash_key"))
        private val BOARD_DESCRIBE_PLAYER_COLOR_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("player_color"))
        private val BOARD_DESCRIBE_FIELD_OFFSET = BOARD_DESCRIBE_LAYOUT.byteOffset(groupElement("field"))

    }

}

internal object RustyRenjuCApi {

    val lib: RustyRenjuC by lazy { RustyRenjuC(NativeLibraryLoader.libraryLookup("rusty_renju_c")) }

    val constants: RustyRenjuC.Constants get() = lib.constants

}
