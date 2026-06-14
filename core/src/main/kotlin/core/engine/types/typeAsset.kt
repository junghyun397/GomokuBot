package core.engine.types

import kotlinx.serialization.Serializable

typealias Pos = String

typealias Color = String

typealias LongInt = Long

typealias RuleKind = String

@Serializable
data class Duration(
    val secs: Long,
    val nanos: Long
)
