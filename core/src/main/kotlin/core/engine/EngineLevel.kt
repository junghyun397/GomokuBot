package core.engine

import core.engine.types.Config
import core.engine.types.Timer
import utils.Identifiable

val BASE_TIMER = Timer(
    time_unit = "Nodes",
    total_remaining = null,
    increment = "0",
    turn = "1000"
)

val BASE_CONFIG = Config(
    draw_condition = null,
    max_depth = null,
    tt_size = 1024 * 1024 * 32,
    workers = 2U,
    pondering = false,
    initial_timer = BASE_TIMER,
    spawn_depth_specialist = false,
)

val ELO_RATINGS by lazy {
    EngineLevel.entries.associateWith { it.rating.rating.toDouble() }
}

enum class EngineLevel(
    override val id: Short,
    val config: Config,
    val rating: EloRating,
    val streaming: Boolean,
    val randomOpening: Boolean,
) : Identifiable {
    AMOEBA(
        id = 0,
        config = BASE_CONFIG.copy(
            workers = 1U,
            tt_size = 1024 * 1024 * 8,
            max_depth = 6,
            max_quiescence_depth = 4,
            initial_timer = BASE_TIMER.copy(
                turn = "10"
            ),
        ),
        rating = EloRating(600.0F),
        streaming = false,
        randomOpening = false,
    ),
    APE(
        id = 1,
        config = BASE_CONFIG.copy(
            workers = 1U,
            tt_size = 1024 * 1024 * 16,
            max_depth = 12,
            max_quiescence_depth = 12,
            initial_timer = BASE_TIMER.copy(
                turn = "400"
            ),
        ),
        rating = EloRating(1000.0F),
        streaming = false,
        randomOpening = false,
    ),
    BEGINNER(
        id = 2,
        config = BASE_CONFIG.copy(
            workers = 1U,
            tt_size = 1024 * 1024 * 32,
            max_quiescence_depth = 24,
            initial_timer = BASE_TIMER.copy(
                turn = "1200"
            ),
        ),
        rating = EloRating(1200.0F),
        streaming = false,
        randomOpening = false,
    ),
    MODERATE(
        id = 3,
        config = BASE_CONFIG.copy(
            workers = 2U,
            tt_size = 1024 * 1024 * 64,
            initial_timer = BASE_TIMER.copy(
                turn = "4000"
            ),
        ),
        rating = EloRating(1400.0F),
        streaming = true,
        randomOpening = false,
    ),
    EXPERT(
        id = 4,
        config = BASE_CONFIG.copy(
            workers = 4U,
            tt_size = 1024 * 1024 * 128,
            initial_timer = BASE_TIMER.copy(
                turn = "8000"
            ),
        ),
        rating = EloRating(1600.0F),
        streaming = true,
        randomOpening = true,
    ),
    GURU(
        id = 5,
        config = BASE_CONFIG.copy(
            workers = 4U,
            tt_size = 1024 * 1024 * 128,
            initial_timer = BASE_TIMER.copy(
                turn = "16000"
            ),
        ),
        rating = EloRating(2000.0F),
        streaming = true,
        randomOpening = true,
    ),
    SAGE(
        id = 6,
        config = BASE_CONFIG.copy(
            workers = 4U,
            tt_size = 1024 * 1024 * 128,
            initial_timer = BASE_TIMER.copy(
                turn = "32000"
            )
        ),
        rating = EloRating(2400.0F),
        streaming = true,
        randomOpening = true,
    )
}
