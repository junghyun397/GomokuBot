package renju.notation

import renju.native.RustyRenjuCApi

enum class ForbiddenKind {
    DoubleThree,
    DoubleFour,
    Overline;

    fun nativeFlag(): Byte =
        when (this) {
            DoubleThree -> RustyRenjuCApi.constants.forbiddenDoubleThree
            DoubleFour -> RustyRenjuCApi.constants.forbiddenDoubleFour
            Overline -> RustyRenjuCApi.constants.forbiddenOverline
        }

    fun fieldFlag(): Byte = (-nativeFlag().toInt()).toByte()

    fun symbol(): Char =
        when (this) {
            DoubleThree -> '3'
            DoubleFour -> '4'
            Overline -> '6'
        }

    companion object {

        fun fromNativeFlag(flag: Byte): ForbiddenKind? =
            when (flag) {
                RustyRenjuCApi.constants.forbiddenDoubleThree -> DoubleThree
                RustyRenjuCApi.constants.forbiddenDoubleFour -> DoubleFour
                RustyRenjuCApi.constants.forbiddenOverline -> Overline
                else -> null
            }

        fun fromFieldFlag(flag: Byte): ForbiddenKind? =
            when (flag) {
                (-RustyRenjuCApi.constants.forbiddenDoubleThree.toInt()).toByte() -> DoubleThree
                (-RustyRenjuCApi.constants.forbiddenDoubleFour.toInt()).toByte() -> DoubleFour
                (-RustyRenjuCApi.constants.forbiddenOverline.toInt()).toByte() -> Overline
                else -> null
            }

    }

}
