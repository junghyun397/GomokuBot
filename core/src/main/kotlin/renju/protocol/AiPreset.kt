package renju.protocol

data class AiPreset(
    val maxNodes: Int,
    val depth: Int,
    val branch: Int,
    val reserve: Int,
)
