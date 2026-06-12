package renju

import renju.native.RustyRenjuCApi
import renju.notation.Pos
import renju.notation.toStringOrNone

@JvmInline value class History(val sequence: List<Pos?>) {

    val moves: Int get() = this.sequence.size

    val started: Boolean get() = this.sequence.isNotEmpty()

    val lastAction: Pos? get() = this.sequence.lastOrNull()

    val isEmpty: Boolean get() = this.sequence.isEmpty()

    fun play(pos: Pos): History =
        History(this.sequence + pos)

    fun pass(): History =
        History(this.sequence + null)

    fun undo(): History =
        History(this.sequence.dropLast(1))

    operator fun get(index: Int): Pos? =
        this.sequence[index]

    operator fun contains(action: Pos?): Boolean =
        action in this.sequence

    fun toMaybePosBuffer(): IntArray? =
        if (this.sequence.isEmpty()) {
            null
        } else {
            IntArray(this.sequence.size) { index ->
                this.sequence[index]?.idx ?: RustyRenjuCApi.constants.posNone
            }
        }

    fun toStringList(): List<String> =
        this.sequence.map { it.toStringOrNone() }

    companion object {

        fun empty(): History = History(emptyList())

        fun of(actions: List<Pos?>): History = History(actions)

    }

}
