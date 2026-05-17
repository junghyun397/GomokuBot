package core.mintaka

import core.mintaka.types.Config
import core.mintaka.types.DurationSchema
import core.mintaka.types.Timer
import utils.structs.Identifiable

val BASE_CONFIG = Config(
    rule_kind = "Renju",
    draw_condition = 225U,
    max_nodes_in_1k = 1_000U,
    max_depth = null,
    max_vcf_depth = 8,
    tt_size = 1024 * 1024 * 64,
    workers = 2U,
    pondering = false,
    dynamic_time = false,
    initial_timer = Timer(
        total_remaining = null,
        increment = DurationSchema(
            secs = 0,
            nanos = 0U,
        ),
        turn = null
    ),
    spawn_depth_specialist = false,
)

enum class EngineLevel(override val id: Short, val config: Config) : Identifiable {
    AMOEBA(
        id = 0,
        config = BASE_CONFIG,
    ),
    APE(
        id = 1,
        config = BASE_CONFIG.copy(
            max_nodes_in_1k = 4_000U,
            max_vcf_depth = 12,
        ),
    ),
    BEGINNER(
        id = 2,
        config = BASE_CONFIG.copy(
            max_nodes_in_1k = 8_000U,
            max_vcf_depth = 16,
            tt_size = 1024 * 1024 * 128,
        ),
    ),
    MODERATE(
        id = 3,
        config = BASE_CONFIG.copy(
            max_nodes_in_1k = 16_000U,
            max_vcf_depth = 16,
            tt_size = 1024 * 1024 * 128,
        ),
    ),
    EXPERT(
        id = 4,
        config = BASE_CONFIG.copy(
            max_nodes_in_1k = 32_000U,
            max_vcf_depth = 24,
            tt_size = 1024 * 1024 * 256,
        ),
    ),
    GURU(
        id = 5,
        config = BASE_CONFIG.copy(
            max_nodes_in_1k = 128_000U,
            max_vcf_depth = 24,
            tt_size = 1024 * 1024 * 256,
        ),
    )
}
