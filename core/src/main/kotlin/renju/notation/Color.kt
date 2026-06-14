package renju.notation

import renju.native.RustyRenjuCApi

enum class Color {
    BLACK,
    WHITE;

    operator fun not(): Color =
        when (this) {
            BLACK -> WHITE
            WHITE -> BLACK
        }

    override fun toString(): String =
        when (this) {
            BLACK -> "Black"
            WHITE -> "White"
        }

    companion object {

        fun from(raw: Byte?): Color? =
            when (raw) {
                RustyRenjuCApi.constants.colorBlack -> BLACK
                RustyRenjuCApi.constants.colorWhite -> WHITE
                else -> null
            }

        fun random(): Color =
            if (Math.random() < 0.5) BLACK
            else WHITE

    }

}

fun Color?.toByte() =
    when (this) {
        Color.BLACK -> RustyRenjuCApi.constants.colorBlack
        Color.WHITE -> RustyRenjuCApi.constants.colorWhite
        else -> RustyRenjuCApi.constants.colorNone
    }.toShort()
