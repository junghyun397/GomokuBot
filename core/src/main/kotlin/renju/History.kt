package renju

import renju.native.RustyRenjuCApi
import renju.notation.Pos

@JvmInline value class History(val inner: List<Pos?>) {

    val moves: Int get() = this.inner.size

    val lastAction: Pos? get() = this.inner.lastOrNull()

    val isEmpty: Boolean get() = this.inner.isEmpty()

    fun play(pos: Pos): History =
        History(this.inner + pos)

    fun pass(): History =
        History(this.inner + null)

    fun undo(): History =
        History(this.inner.dropLast(1))

    operator fun get(index: Int): Pos? =
        this.inner[index]

    operator fun contains(action: Pos?): Boolean =
        action in this.inner

    fun toMaybePosBuffer(): ByteArray? =
        if (this.inner.isEmpty()) {
            null
        } else {
            ByteArray(this.inner.size) { index ->
                this.inner[index]?.idx?.toByte() ?: RustyRenjuCApi.constants.posNone
            }
        }

    companion object {

        fun empty(): History = History(emptyList())

        fun of(actions: List<Pos?>): History = History(actions)

    }

}
