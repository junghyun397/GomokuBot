package core.mintaka.types

import renju.notation.HashKey
import renju.notation.Pos

fun BoardData.convert(): Board {
    TODO()
}

fun hashKey(raw: core.mintaka.types.HashKey): HashKey {
    return HashKey(raw)
}

fun pos(raw: core.mintaka.types.Pos): Pos {
    return Pos.fromCartesian(raw)!!
}
