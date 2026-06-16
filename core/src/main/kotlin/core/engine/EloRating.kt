package core.engine

import kotlin.math.pow

@JvmInline value class EloRating(val rating: Float) {

    enum class MatchResult(val wld: Float) {
        WIN(1.0f), LOSE(0.0f), DRAW(0.5f),
    }

    fun delta(opponent: EloRating, result: MatchResult, kFactor: Float = 16f): Float {
        val expectedWld = 1.0f / (1.0f + 10.0f.pow((opponent.rating - this.rating) / 400.0f))

        return kFactor * (result.wld - expectedWld)
    }

    operator fun plus(delta: Float) = EloRating(this.rating + delta)

    companion object {

        val STARTING_RATING = EloRating(600.0f)

    }

}
