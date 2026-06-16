package renju

import renju.native.RustyRenjuCApi
import renju.notation.Pos

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
@JvmInline value class History(val sequence: List<Pos?>) : List<Pos?> by sequence {

    fun play(pos: Pos?): History =
        History(this.sequence + pos)

    fun undo(): History =
        History(this.sequence.dropLast(1))

    fun toMaybePosBuffer(): IntArray? =
        if (this.sequence.isEmpty()) {
            null
        } else {
            IntArray(this.sequence.size) { index ->
                this.sequence[index]?.idx ?: RustyRenjuCApi.constants.posNone
            }
        }

    companion object {

        fun empty(): History = History(emptyList())

        fun of(actions: List<Pos?>): History = History(actions)

    }

}
