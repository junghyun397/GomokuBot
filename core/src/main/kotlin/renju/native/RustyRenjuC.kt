package renju.native

import renju.notation.Pos
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.invoke.MethodHandle

internal class RustyRenjuC internal constructor(
    lookup: SymbolLookup,
) {

    private val linker = Linker.nativeLinker()

    private val rustyRenjuColorBlack = downcall(lookup, "rusty_renju_color_black", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuColorWhite = downcall(lookup, "rusty_renju_color_white", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuColorNone = downcall(lookup, "rusty_renju_color_none", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuForbiddenKindNone = downcall(lookup, "rusty_renju_forbidden_kind_none", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuForbiddenKindDoubleThree = downcall(lookup, "rusty_renju_forbidden_kind_double_three", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuForbiddenKindDoubleFour = downcall(lookup, "rusty_renju_forbidden_kind_double_four", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuForbiddenKindDoubleOverline = downcall(lookup, "rusty_renju_forbidden_kind_double_overline", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuPosNone = downcall(lookup, "rusty_renju_pos_none", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuBoardExportItemEmpty = downcall(lookup, "rusty_renju_board_export_item_empty", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuBoardExportItemStone = downcall(lookup, "rusty_renju_board_export_item_stone", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuBoardExportItemForbidden = downcall(lookup, "rusty_renju_board_export_item_forbidden", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuClosedFourMask = downcall(lookup, "rusty_renju_closed_four_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))
    private val rustyRenjuOpenFourMask = downcall(lookup, "rusty_renju_open_four_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))
    private val rustyRenjuFiveMask = downcall(lookup, "rusty_renju_five_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))
    private val rustyRenjuOpenThreeMask = downcall(lookup, "rusty_renju_open_three_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))
    private val rustyRenjuCloseThreeMask = downcall(lookup, "rusty_renju_close_three_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))
    private val rustyRenjuPotentialMask = downcall(lookup, "rusty_renju_potential_mask", FunctionDescriptor.of(ValueLayout.JAVA_INT))

    private val rustyRenjuDefaultBoard = downcall(lookup, "rusty_renju_default_board", FunctionDescriptor.of(ValueLayout.ADDRESS))
    private val rustyRenjuBoardFromHistory = downcall(
        lookup,
        "rusty_renju_board_from_history",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG),
    )
    private val rustyRenjuBoardFromString = downcall(
        lookup,
        "rusty_renju_board_from_string",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS),
    )
    private val rustyRenjuBoardToString = downcall(
        lookup,
        "rusty_renju_board_to_string",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS),
    )

    private val rustyRenjuBoardPlayerColor = downcall(
        lookup,
        "rusty_renju_board_player_color",
        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS),
    )
    private val rustyRenjuBoardStones = downcall(
        lookup,
        "rusty_renju_board_stones",
        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS),
    )
    private val rustyRenjuBoardPattern = downcall(
        lookup,
        "rusty_renju_board_pattern",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE),
    )
    private val rustyRenjuBoardIsPosEmpty = downcall(
        lookup,
        "rusty_renju_board_is_pos_empty",
        FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE),
    )
    private val rustyRenjuBoardIsLegalMove = downcall(
        lookup,
        "rusty_renju_board_is_legal_move",
        FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE),
    )
    private val rustyRenjuBoardStoneKind = downcall(
        lookup,
        "rusty_renju_board_stone_kind",
        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE),
    )

    private val rustyRenjuBoardSet = downcall(
        lookup,
        "rusty_renju_board_set",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE),
    )
    private val rustyRenjuBoardUnset = downcall(
        lookup,
        "rusty_renju_board_unset",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE),
    )
    private val rustyRenjuBoardFree = downcall(
        lookup,
        "rusty_renju_board_free",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
    )

    private val rustyRenjuBoardDescribe = downcall(
        lookup,
        "rusty_renju_board_describe",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG),
    )
    private val rustyRenjuBoardDescribeFree = downcall(
        lookup,
        "rusty_renju_board_describe_free",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
    )

    fun rusty_renju_color_black(): Byte = rustyRenjuColorBlack.callByte()

    fun rusty_renju_color_white(): Byte = rustyRenjuColorWhite.callByte()

    fun rusty_renju_color_none(): Byte = rustyRenjuColorNone.callByte()

    fun rusty_renju_forbidden_kind_none(): Byte = rustyRenjuForbiddenKindNone.callByte()

    fun rusty_renju_forbidden_kind_double_three(): Byte = rustyRenjuForbiddenKindDoubleThree.callByte()

    fun rusty_renju_forbidden_kind_double_four(): Byte = rustyRenjuForbiddenKindDoubleFour.callByte()

    fun rusty_renju_forbidden_kind_double_overline(): Byte = rustyRenjuForbiddenKindDoubleOverline.callByte()

    fun rusty_renju_pos_none(): Byte = rustyRenjuPosNone.callByte()

    fun rusty_renju_board_export_item_empty(): Byte = rustyRenjuBoardExportItemEmpty.callByte()

    fun rusty_renju_board_export_item_stone(): Byte = rustyRenjuBoardExportItemStone.callByte()

    fun rusty_renju_board_export_item_forbidden(): Byte = rustyRenjuBoardExportItemForbidden.callByte()

    fun rusty_renju_closed_four_mask(): Int = rustyRenjuClosedFourMask.callInt()

    fun rusty_renju_open_four_mask(): Int = rustyRenjuOpenFourMask.callInt()

    fun rusty_renju_five_mask(): Int = rustyRenjuFiveMask.callInt()

    fun rusty_renju_open_three_mask(): Int = rustyRenjuOpenThreeMask.callInt()

    fun rusty_renju_close_three_mask(): Int = rustyRenjuCloseThreeMask.callInt()

    fun rusty_renju_potential_mask(): Int = rustyRenjuPotentialMask.callInt()

    fun rusty_renju_default_board(): MemorySegment? =
        rustyRenjuDefaultBoard.callAddress().nullIfNull()

    fun rusty_renju_board_from_history(actions: ByteArray?, len: Long): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            val actionSegment = actions.toNativeSegmentOrNull(arena)
            rustyRenjuBoardFromHistory.callAddress(actionSegment, len).nullIfNull()
        }
    }

    fun rusty_renju_board_from_string(source: String?): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            val sourceSegment = source?.let { arena.allocateCString(it) } ?: MemorySegment.NULL
            rustyRenjuBoardFromString.callAddress(sourceSegment).nullIfNull()
        }
    }

    fun rusty_renju_board_to_string(board: MemorySegment?): MemorySegment? =
        rustyRenjuBoardToString.callAddress(board.orNullAddress()).nullIfNull()

    fun rusty_renju_board_player_color(board: MemorySegment?): Byte =
        rustyRenjuBoardPlayerColor.callByte(board.orNullAddress())

    fun rusty_renju_board_stones(board: MemorySegment?): Byte =
        rustyRenjuBoardStones.callByte(board.orNullAddress())

    fun rusty_renju_board_pattern(board: MemorySegment?, color: Byte, pos: Byte): Int =
        rustyRenjuBoardPattern.callInt(board.orNullAddress(), color, pos)

    fun rusty_renju_board_is_pos_empty(board: MemorySegment?, pos: Byte): Boolean =
        rustyRenjuBoardIsPosEmpty.callBoolean(board.orNullAddress(), pos)

    fun rusty_renju_board_is_legal_move(board: MemorySegment?, pos: Byte): Boolean =
        rustyRenjuBoardIsLegalMove.callBoolean(board.orNullAddress(), pos)

    fun rusty_renju_board_stone_kind(board: MemorySegment?, pos: Byte): Byte =
        rustyRenjuBoardStoneKind.callByte(board.orNullAddress(), pos)

    fun rusty_renju_board_set(board: MemorySegment?, pos: Byte): MemorySegment? =
        rustyRenjuBoardSet.callAddress(board.orNullAddress(), pos).nullIfNull()

    fun rusty_renju_board_unset(board: MemorySegment?, pos: Byte): MemorySegment? =
        rustyRenjuBoardUnset.callAddress(board.orNullAddress(), pos).nullIfNull()

    fun rusty_renju_board_free(board: MemorySegment?) {
        rustyRenjuBoardFree.callVoid(board.orNullAddress())
    }

    fun rusty_renju_board_describe(board: MemorySegment?, maybePosSlice: ByteArray?, len: Long): MemorySegment? {
        return Arena.ofConfined().use { arena ->
            val maybePosSegment = maybePosSlice.toNativeSegmentOrNull(arena)
            rustyRenjuBoardDescribe.callAddress(board.orNullAddress(), maybePosSegment, len).nullIfNull()
        }
    }

    fun rusty_renju_board_describe_free(describe: MemorySegment?) {
        rustyRenjuBoardDescribeFree.callVoid(describe.orNullAddress())
    }

    data class BoardExportStone(
        val color: Byte,
        val sequence: Byte,
    )

    data class BoardExportItem(
        val kind: Byte,
        val stone: BoardExportStone,
        val forbidden_kind: Byte,
    )

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

    private fun downcall(
        lookup: SymbolLookup,
        symbolName: String,
        descriptor: FunctionDescriptor,
    ): MethodHandle {
        val symbol = lookup.find(symbolName)
            .orElseThrow { IllegalStateException("Native symbol '$symbolName' not found") }

        return linker.downcallHandle(symbol, descriptor)
    }

    private fun Arena.allocateCString(value: String): MemorySegment {
        val encoded = value.toByteArray(Charsets.UTF_8)
        val segment = allocate((encoded.size + 1).toLong())

        for (idx in encoded.indices) {
            segment.set(ValueLayout.JAVA_BYTE, idx.toLong(), encoded[idx])
        }
        segment.set(ValueLayout.JAVA_BYTE, encoded.size.toLong(), 0)

        return segment
    }

    private fun ByteArray?.toNativeSegmentOrNull(arena: Arena): MemorySegment {
        val data = this ?: return MemorySegment.NULL
        if (data.isEmpty()) {
            return MemorySegment.NULL
        }

        val segment = arena.allocate(data.size.toLong())
        for (idx in data.indices) {
            segment.set(ValueLayout.JAVA_BYTE, idx.toLong(), data[idx])
        }

        return segment
    }

    private fun MemorySegment?.orNullAddress(): MemorySegment = this ?: MemorySegment.NULL

    private fun MemorySegment.nullIfNull(): MemorySegment? =
        if (this == MemorySegment.NULL) null else this

    private fun MethodHandle.callByte(vararg args: Any): Byte =
        invokeWithArguments(*args) as Byte

    private fun MethodHandle.callInt(vararg args: Any): Int =
        invokeWithArguments(*args) as Int

    private fun MethodHandle.callBoolean(vararg args: Any): Boolean =
        invokeWithArguments(*args) as Boolean

    private fun MethodHandle.callAddress(vararg args: Any): MemorySegment =
        invokeWithArguments(*args) as MemorySegment

    private fun MethodHandle.callVoid(vararg args: Any) {
        invokeWithArguments(*args)
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

    val lib: RustyRenjuC by lazy {
        RustyRenjuC(NativeLibraryLoader.libraryLookup("rusty_renju_c"))
    }

    val constants: Constants by lazy {
        Constants(
            colorBlack = lib.rusty_renju_color_black(),
            colorWhite = lib.rusty_renju_color_white(),
            colorNone = lib.rusty_renju_color_none(),
            forbiddenNone = lib.rusty_renju_forbidden_kind_none(),
            forbiddenDoubleThree = lib.rusty_renju_forbidden_kind_double_three(),
            forbiddenDoubleFour = lib.rusty_renju_forbidden_kind_double_four(),
            forbiddenOverline = lib.rusty_renju_forbidden_kind_double_overline(),
            posNone = lib.rusty_renju_pos_none(),
            exportItemEmpty = lib.rusty_renju_board_export_item_empty(),
            exportItemStone = lib.rusty_renju_board_export_item_stone(),
            exportItemForbidden = lib.rusty_renju_board_export_item_forbidden(),
            closedFourMask = lib.rusty_renju_closed_four_mask(),
            openFourMask = lib.rusty_renju_open_four_mask(),
            fiveMask = lib.rusty_renju_five_mask(),
            openThreeMask = lib.rusty_renju_open_three_mask(),
            closeThreeMask = lib.rusty_renju_close_three_mask(),
            potentialMask = lib.rusty_renju_potential_mask(),
        )
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

}
