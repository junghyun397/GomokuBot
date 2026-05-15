package core.mintaka

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
typealias Bitfield = String

typealias HashKey = String

typealias Color = ColorSchema

typealias ColorContainer<T> = List<T>

@Serializable
data class BoardData (
	val hash_key: HashKey,
	val player_color: Color,
	val bitfield: ColorContainer<Bitfield>
)

typealias Board = BoardData

@Serializable
sealed class BoardExportItemSchema {
	@Serializable
	@SerialName("Stone")
	data class Stone(val content: BoardExportStone): BoardExportItemSchema()
	@Serializable
	@SerialName("Empty")
	object Empty: BoardExportItemSchema()
	@Serializable
	@SerialName("Forbidden")
	data class Forbidden(val content: ForbiddenKind): BoardExportItemSchema()
}

typealias BoardExportItem = BoardExportItemSchema

typealias ByteSize = LongInt

/// Generated type representing the anonymous struct variant `Play` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaPlayInner (
	val hash: HashKey,
	val pos: MaybePos
)

/// Generated type representing the anonymous struct variant `Set` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaSetInner (
	val hash: HashKey,
	val pos: Pos,
	val color: Color
)

/// Generated type representing the anonymous struct variant `Unset` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaUnsetInner (
	val hash: HashKey,
	val pos: Pos,
	val color: Color
)

/// Generated type representing the anonymous struct variant `Undo` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaUndoInner (
	val hash: HashKey
)

/// Generated type representing the anonymous struct variant `BatchSet` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaBatchSetInner (
	val player_moves: List<Pos>,
	val opponent_moves: List<Pos>
)

/// Generated type representing the anonymous struct variant `MaxNodes` of the `CommandSchema` Rust enum
@Serializable
data class CommandSchemaMaxNodesInner (
	val in_1k: UInt
)

@Serializable
sealed class CommandSchema {
	@Serializable
	@SerialName("Clear")
	object Clear: CommandSchema()
	@Serializable
	@SerialName("Load")
	data class Load(val content: CompactGameState): CommandSchema()
	@Serializable
	@SerialName("Sync")
	data class Sync(val content: CompactGameState): CommandSchema()
	@Serializable
	@SerialName("Play")
	data class Play(val content: CommandSchemaPlayInner): CommandSchema()
	@Serializable
	@SerialName("Set")
	data class Set(val content: CommandSchemaSetInner): CommandSchema()
	@Serializable
	@SerialName("Unset")
	data class Unset(val content: CommandSchemaUnsetInner): CommandSchema()
	@Serializable
	@SerialName("Undo")
	data class Undo(val content: CommandSchemaUndoInner): CommandSchema()
	@Serializable
	@SerialName("BatchSet")
	data class BatchSet(val content: CommandSchemaBatchSetInner): CommandSchema()
	@Serializable
	@SerialName("TurnTime")
	data class TurnTime(val content: DurationSchema): CommandSchema()
	@Serializable
	@SerialName("IncrementTime")
	data class IncrementTime(val content: DurationSchema): CommandSchema()
	@Serializable
	@SerialName("TotalTime")
	data class TotalTime(val content: DurationSchema): CommandSchema()
	@Serializable
	@SerialName("ConsumeTime")
	data class ConsumeTime(val content: DurationSchema): CommandSchema()
	@Serializable
	@SerialName("Pondering")
	data class Pondering(val content: Boolean): CommandSchema()
	@Serializable
	@SerialName("MaxNodes")
	data class MaxNodes(val content: CommandSchemaMaxNodesInner): CommandSchema()
	@Serializable
	@SerialName("Workers")
	data class Workers(val content: UInt): CommandSchema()
	@Serializable
	@SerialName("MaxMemory")
	data class MaxMemory(val content: ByteSize): CommandSchema()
	@Serializable
	@SerialName("Rule")
	data class Rule(val content: RuleKind): CommandSchema()
	@Serializable
	@SerialName("Config")
	data class Config(val content: Config): CommandSchema()
}

typealias Command = CommandSchema

typealias Depth = Int

typealias ForbiddenKind = String

@Serializable
sealed class GameResultSchema {
	@Serializable
	@SerialName("Win")
	data class Win(val content: Color): GameResultSchema()
	@Serializable
	@SerialName("Draw")
	object Draw: GameResultSchema()
	@Serializable
	@SerialName("Full")
	object Full: GameResultSchema()
}

typealias GameResult = GameResultSchema

typealias Pos = PosSchema

typealias MaybePos = Pos?

typealias History = List<MaybePos>

@Serializable
data class GameStateData (
	val board: Board,
	val history: History
)

typealias GameState = GameStateData

typealias PrincipalVariation = List<MaybePos>

/// Generated type representing the anonymous struct variant `Status` of the `ResponseSchema` Rust enum
@Serializable
data class ResponseSchemaStatusInner (
	val hash: HashKey,
	val best_move: MaybePos,
	val score: Score,
	val selective_depth: Depth,
	val total_nodes_in_1k: UInt,
	val pv: PrincipalVariation,
	val time_elapsed: DurationSchema
)

@Serializable
sealed class ResponseSchema {
	@Serializable
	@SerialName("Begins")
	data class Begins(val content: ComputingResource): ResponseSchema()
	@Serializable
	@SerialName("Status")
	data class Status(val content: ResponseSchemaStatusInner): ResponseSchema()
}

typealias Response = ResponseSchema

typealias RuleKind = String

typealias Score = Int

typealias SearchObjective = String

@Serializable
data class DurationSchema (
	val secs: LongInt,
	val nanos: UInt
)

@Serializable
data class BestMove (
	val position_hash: HashKey,
	val best_move: MaybePos,
	val score: Score,
	val selective_depth: UInt,
	val total_nodes_in_1k: UInt,
	val pv: PrincipalVariation,
	val time_elapsed: DurationSchema
)

@Serializable
data class BoardDescribe (
	val hash_key: HashKey,
	val player_color: Color,
	val field: List<BoardExportItem>,
	val winner: Color? = null
)

@Serializable
data class BoardExportStone (
	val color: Color,
	val sequence: UByte
)

@Serializable
data class CommandResult (
	val hash_key: HashKey,
	val result: GameResult? = null
)

@Serializable
data class CompactGameState (
	val board: Board,
	val history: History
)

@Serializable
data class ComputingResource (
	val workers: UInt,
	val tt_size: ByteSize,
	val time: DurationSchema,
	val nodes_in_1k: UInt? = null
)

@Serializable
data class Timer (
	val total_remaining: DurationSchema? = null,
	val increment: DurationSchema,
	val turn: DurationSchema? = null
)

@Serializable
data class Config (
	val rule_kind: RuleKind,
	val draw_condition: UInt,
	val max_nodes_in_1k: UInt? = null,
	val max_depth: Depth? = null,
	val max_vcf_depth: Depth? = null,
	val tt_size: ByteSize,
	val workers: UInt,
	val pondering: Boolean,
	val dynamic_time: Boolean,
	val initial_timer: Timer,
	val spawn_depth_specialist: Boolean
)

@Serializable
data class CreateSessionRequest (
	val api_password: String? = null,
	val config: Config? = null,
	val state: GameState
)

@Serializable
data class CreateSessionResponse (
	val sid: String,
	val token: String,
	val hash: String,
	val version: String
)

@Serializable
data class Health (
	val available_workers: UInt
)

@Serializable
data class LaunchSessionRequest (
	val position_hash: HashKey,
	val nodes_polling_interval_in_ms: UInt? = null
)

