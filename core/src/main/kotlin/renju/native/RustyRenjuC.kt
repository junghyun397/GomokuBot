package renju.native

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import renju.notation.Pos

internal interface RustyRenjuC : Library {

    fun rusty_renju_color_black(): Byte

    fun rusty_renju_color_white(): Byte

    fun rusty_renju_color_none(): Byte

    fun rusty_renju_forbidden_kind_none(): Byte

    fun rusty_renju_forbidden_kind_double_three(): Byte

    fun rusty_renju_forbidden_kind_double_four(): Byte

    fun rusty_renju_forbidden_kind_double_overline(): Byte

    fun rusty_renju_pos_none(): Byte

    fun rusty_renju_board_export_item_empty(): Byte

    fun rusty_renju_board_export_item_stone(): Byte

    fun rusty_renju_board_export_item_forbidden(): Byte

    fun rusty_renju_closed_four_mask(): Int

    fun rusty_renju_open_four_mask(): Int

    fun rusty_renju_five_mask(): Int

    fun rusty_renju_open_three_mask(): Int

    fun rusty_renju_close_three_mask(): Int

    fun rusty_renju_potential_mask(): Int

    fun rusty_renju_default_board(): Pointer?

    fun rusty_renju_board_from_history(actions: ByteArray?, len: Long): Pointer?

    fun rusty_renju_board_from_string(source: String?): Pointer?

    fun rusty_renju_board_to_string(board: Pointer?): Pointer?

    fun rusty_renju_board_player_color(board: Pointer?): Byte

    fun rusty_renju_board_stones(board: Pointer?): Byte

    fun rusty_renju_board_pattern(board: Pointer?, color: Byte, pos: Byte): Int

    fun rusty_renju_board_is_pos_empty(board: Pointer?, pos: Byte): Boolean

    fun rusty_renju_board_is_legal_move(board: Pointer?, pos: Byte): Boolean

    fun rusty_renju_board_stone_kind(board: Pointer?, pos: Byte): Byte

    fun rusty_renju_board_set(board: Pointer?, pos: Byte): Pointer?

    fun rusty_renju_board_unset(board: Pointer?, pos: Byte): Pointer?

    fun rusty_renju_board_free(board: Pointer?)

    fun rusty_renju_board_describe(board: Pointer?, maybePosSlice: Pointer?, len: Long): Pointer?

    fun rusty_renju_board_describe_free(describe: Pointer?)

    @Structure.FieldOrder("color", "sequence")
    class BoardExportStone : Structure {

        @JvmField
        var color: Byte = 0

        @JvmField
        var sequence: Byte = 0

        constructor() : super()

        constructor(pointer: Pointer?) : super(pointer) {
            read()
        }

    }

    @Structure.FieldOrder("kind", "stone", "forbidden_kind")
    class BoardExportItem : Structure {

        @JvmField
        var kind: Byte = 0

        @JvmField
        var stone: BoardExportStone = BoardExportStone()

        @JvmField
        var forbidden_kind: Byte = 0

        constructor() : super()

        constructor(pointer: Pointer?) : super(pointer) {
            read()
        }

    }

    @Structure.FieldOrder("hash_key", "player_color", "field")
    class BoardDescribe : Structure {

        @JvmField
        var hash_key: Long = 0

        @JvmField
        var player_color: Byte = 0

        @JvmField
        var field: Array<BoardExportItem> = Array(Pos.BOARD_SIZE) { BoardExportItem() }

        constructor() : super()

        constructor(pointer: Pointer?) : super(pointer) {
            read()
        }

    }

}

internal object RustyRenjuCApi {

    val lib: RustyRenjuC by lazy {
        NativeLibraryLoader.ensureLoaded()
        Native.load("rusty_renju_c", RustyRenjuC::class.java)
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
