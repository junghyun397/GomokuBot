package core.assets

import renju.notation.ForbiddenKind

fun forbiddenKindToText(kind: ForbiddenKind?) =
    when (kind) {
        ForbiddenKind.DoubleThree -> "3-3"
        ForbiddenKind.DoubleFour -> "4-4"
        ForbiddenKind.Overline -> "≥6"
        null -> "UNKNOWN"
    }
