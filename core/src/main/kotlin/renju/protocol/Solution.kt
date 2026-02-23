package renju.protocol

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

sealed interface Solution {

    fun idx(): Int

    companion object {

        fun fromIterable(values: Iterable<Int>): Option<Solution> {
            val sequence = values.toList()
            if (sequence.isEmpty()) {
                return None
            }

            fun build(index: Int): Solution {
                val move = sequence[index]
                return if (index == sequence.lastIndex) {
                    SolutionLeaf(move)
                } else {
                    val child = build(index + 1)
                    SolutionNode(move, mapOf(child.idx() to child))
                }
            }

            return Some(build(0))
        }

    }

}

open class SolutionLeaf(private val move: Int) : Solution {

    override fun idx(): Int = move

}

class SolutionNode(
    move: Int,
    private val children: Map<Int, Solution> = emptyMap(),
) : SolutionLeaf(move) {

    fun child(): ChildAccessor = ChildAccessor(children)

    class ChildAccessor(private val children: Map<Int, Solution>) {

        fun get(move: Int): Option<Solution> =
            Option.fromNullable(children[move])

    }

}
