package core.inference

import renju.protocol.AiPreset
import utils.structs.Identifiable

private val amoebaPreset = AiPreset(225, 1, 10, 0)
private val apePreset = AiPreset(10_000, 7, 10, 4)
private val beginnerPreset = AiPreset(100_000, 9, 16, 8)
private val moderatePreset = AiPreset(1_000_000, 11, 24, 16)
private val expertPreset = AiPreset(10_000_000, 16, 24, 24)
private val guruPreset = AiPreset(100_000_000, Int.MAX_VALUE, 32, 24)

enum class AiLevel(override val id: Short, val aiPreset: AiPreset) : Identifiable {
    AMOEBA(0, amoebaPreset),
    APE(1, apePreset),
    BEGINNER(2, beginnerPreset),
    MODERATE(3, moderatePreset),
    EXPERT(4, expertPreset),
    GURU(5, guruPreset)
}
