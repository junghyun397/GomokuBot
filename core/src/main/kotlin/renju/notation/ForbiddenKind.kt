package renju.notation

import renju.native.RustyRenju

enum class ForbiddenKind(val value: Byte) {
    DoubleThree(RustyRenju.forbiddenDoubleThree),
    DoubleFour(RustyRenju.forbiddenDoubleFour),
    Overline(RustyRenju.forbiddenOverline);

    companion object {

        fun from(flag: Byte): ForbiddenKind? =
            when (flag) {
                RustyRenju.forbiddenDoubleThree -> DoubleThree
                RustyRenju.forbiddenDoubleFour -> DoubleFour
                RustyRenju.forbiddenOverline -> Overline
                else -> null
            }

    }
}
