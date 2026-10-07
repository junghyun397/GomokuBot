package core.engine

import kotlin.math.pow

@JvmInline value class EloRating(val rating: Float) {

    enum class MatchResult(val wld: Float) {
        WIN(1.0f), LOSE(0.0f), DRAW(0.5f),
    }

    @JvmInline value class Delta(val delta: Float) {

        override fun toString() = String.format("%+.2f", this.delta)

    }

    fun delta(opponent: EloRating, result: MatchResult, kFactor: Float = 32f): Delta {
        val expectedWld = 1.0f / (1.0f + 10.0f.pow((opponent.rating - this.rating) / 400.0f))

        return Delta(kFactor * (result.wld - expectedWld))
    }

    operator fun plus(delta: Delta) = EloRating(this.rating + delta.delta)

    override fun toString() = String.format("%.2f", this.rating)

    companion object {

        val STARTING_RATING = EloRating(600.0f)

    }

}
