package renju.notation

import renju.native.RustyRenjuCApi

enum class ForbiddenKind(val value: Byte) {
    DoubleThree(RustyRenjuCApi.constants.forbiddenDoubleThree),
    DoubleFour(RustyRenjuCApi.constants.forbiddenDoubleFour),
    Overline(RustyRenjuCApi.constants.forbiddenOverline);

    companion object {

        fun from(flag: Byte): ForbiddenKind? =
            when (flag) {
                RustyRenjuCApi.constants.forbiddenDoubleThree -> DoubleThree
                RustyRenjuCApi.constants.forbiddenDoubleFour -> DoubleFour
                RustyRenjuCApi.constants.forbiddenOverline -> Overline
                else -> null
            }

    }
}
