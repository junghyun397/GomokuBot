package renju.notation

import renju.native.RustyRenjuCApi

enum class Color(val naiveFlag: Byte) {
    Black(RustyRenjuCApi.constants.colorBlack),
    White(RustyRenjuCApi.constants.colorWhite);

    operator fun not(): Color =
        when (this) {
            Black -> White
            White -> Black
        }

    override fun toString(): String =
        when (this) {
            Black -> "Black"
            White -> "White"
        }

    companion object {

        fun from(raw: Byte): Color =
            if (raw == RustyRenjuCApi.constants.colorWhite) White
            else Black

        fun emptyFlag(): Byte = RustyRenjuCApi.constants.colorNone

    }

}
