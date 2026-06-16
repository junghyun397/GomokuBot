package core.engine

import core.engine.types.Config
import core.engine.types.Duration
import core.engine.types.Timer
import utils.Identifiable

val BASE_CONFIG = Config(
    draw_condition = null,
    max_nodes_in_1k = null,
    max_depth = null,
    max_vcf_depth = null,
    tt_size = 1024 * 1024 * 32,
    workers = 2U,
    pondering = false,
    initial_timer = Timer(
        total_remaining = null,
        increment = Duration(
            secs = 0,
            nanos = 0,
        ),
        turn = null
    ),
    spawn_depth_specialist = false,
)

enum class EngineLevel(override val id: Short, val config: Config, val rating: EloRating, val streaming: Boolean) : Identifiable {
    AMOEBA(
        id = 0,
        config = BASE_CONFIG.copy(
            workers = 2U,
            tt_size = 1024 * 1024 * 8,
            max_nodes_in_1k = 800U,
            max_vcf_depth = 6,
        ),
        rating = EloRating(600.0F),
        streaming = false,
    ),
    APE(
        id = 1,
        config = BASE_CONFIG.copy(
            workers = 2U,
            tt_size = 1024 * 1024 * 16,
            max_nodes_in_1k = 2_000U,
            max_vcf_depth = 10,
        ),
        rating = EloRating(1000.0F),
        streaming = false,
    ),
    BEGINNER(
        id = 2,
        config = BASE_CONFIG.copy(
            workers = 2U,
            tt_size = 1024 * 1024 * 32,
            max_nodes_in_1k = 8_000U,
            max_vcf_depth = 12,
        ),
        rating = EloRating(1200.0F),
        streaming = true,
    ),
    MODERATE(
        id = 3,
        config = BASE_CONFIG.copy(
            workers = 2U,
            tt_size = 1024 * 1024 * 64,
            max_nodes_in_1k = 16_000U,
            max_vcf_depth = 16,
        ),
        rating = EloRating(1400.0F),
        streaming = true,
    ),
    EXPERT(
        id = 4,
        config = BASE_CONFIG.copy(
            workers = 4U,
            tt_size = 1024 * 1024 * 128,
            max_nodes_in_1k = 32_000U,
            max_vcf_depth = 24,
        ),
        rating = EloRating(1600.0F),
        streaming = true,
    ),
    GURU(
        id = 5,
        config = BASE_CONFIG.copy(
            workers = 4U,
            tt_size = 1024 * 1024 * 256,
            max_nodes_in_1k = 128_000U,
            max_vcf_depth = 24,
        ),
        rating = EloRating(2000.0F),
        streaming = true,
    )
}
