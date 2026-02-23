package core.inference

import utils.structs.Identifiable

enum class AiLevel(override val id: Short) : Identifiable {
    AMOEBA(0),
    APE(1),
    BEGINNER(2),
    MODERATE(3),
    EXPERT(4),
    GURU(5)
}
