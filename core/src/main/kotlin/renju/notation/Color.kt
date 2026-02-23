package renju.notation

import renju.native.RustyRenjuCApi

enum class Color {
    Black,
    White;

    fun flag(): Byte =
        when (this) {
            Black -> RustyRenjuCApi.constants.colorBlack
            White -> RustyRenjuCApi.constants.colorWhite
        }

    fun reversed(): Color =
        when (this) {
            Black -> White
            White -> Black
        }

    companion object {

        fun fromNative(raw: Byte): Color =
            if (raw == RustyRenjuCApi.constants.colorWhite) White
            else Black

        fun fromFlag(flag: Byte): Color? =
            when (flag) {
                RustyRenjuCApi.constants.colorBlack -> Black
                RustyRenjuCApi.constants.colorWhite -> White
                else -> null
            }

        fun emptyFlag(): Byte = RustyRenjuCApi.constants.colorNone

        fun isStone(flag: Byte): Boolean = fromFlag(flag) != null

        fun isForbidden(flag: Byte, turn: Color): Boolean =
            turn == Black && ForbiddenKind.fromFieldFlag(flag) != null

    }

}
