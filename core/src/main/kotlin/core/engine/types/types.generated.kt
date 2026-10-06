package core.engine.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
typealias Bitfield = String

typealias RuleKind = String

typealias HashKey = String

typealias ColorContainer<T> = List<T>

@Serializable
data class BoardData (
	val rule_kind: RuleKind,
	val hash_key: HashKey,
	val player_color: Color,
	val bitfield: ColorContainer<Bitfield>
)

typealias Board = BoardData

typealias ByteSize = LongInt

typealias Depth = Int

typealias ForbiddenKind = String

typealias MaybePos = Pos?

typealias History = List<MaybePos>

@Serializable
data class GameStateData (
	val board_data: BoardData,
	val history: History
)

typealias GameState = GameStateData

typealias PrincipalVariation = List<MaybePos>

typealias Score = Int

typealias ThreatSearchKind = String

typealias TimeUnit = String

typealias TimeValue = String

@Serializable
data class Nodes (
	val in_1k: UInt
)

@Serializable
data class BestMove (
	val position_hash: HashKey,
	val best_move: MaybePos,
	val score: Score,
	val selective_depth: Depth,
	val total_nodes: Nodes,
	val pv: PrincipalVariation,
	val time_elapsed: Duration
)

@Serializable
sealed class BoardExportItem {
	@Serializable
	@SerialName("Stone")
	data class Stone(val content: Color): BoardExportItem()
	@Serializable
	@SerialName("Empty")
	object Empty: BoardExportItem()
	@Serializable
	@SerialName("Forbidden")
	data class Forbidden(val content: ForbiddenKind): BoardExportItem()
}

@Serializable
data class BoardWinner (
	val color: Color,
	val components: List<Pos>
)

@Serializable
data class BoardDescribe (
	val hash_key: HashKey,
	val player_color: Color,
	val field: List<BoardExportItem>,
	val winner: BoardWinner? = null
)

@Serializable
sealed class GameResult {
	@Serializable
	@SerialName("Win")
	data class Win(val content: Color): GameResult()
	@Serializable
	@SerialName("Draw")
	object Draw: GameResult()
	@Serializable
	@SerialName("Full")
	object Full: GameResult()
}

@Serializable
data class CommandResult (
	val hash_key: HashKey,
	val result: GameResult? = null
)

@Serializable
data class ComputingResource (
	val workers: UInt,
	val time_unit: TimeUnit,
	val time_limit: TimeValue? = null
)

@Serializable
data class Timer (
	val time_unit: TimeUnit,
	val total_remaining: TimeValue? = null,
	val increment: TimeValue,
	val turn: TimeValue? = null
)

@Serializable
data class Config (
	val draw_condition: UInt? = null,
	val max_depth: Depth? = null,
	val max_quiescence_depth: Depth? = null,
	val tt_size: ByteSize,
	val workers: UInt,
	val pondering: Boolean,
	val initial_timer: Timer,
	val spawn_depth_specialist: Boolean
)

@Serializable
data class CreateSessionRequest (
	val api_password: String? = null,
	val config: Config? = null,
	val state: GameState,
	val time_to_hibernate: Duration? = null,
	val time_to_live: Duration? = null
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
	val version: String,
	val available_workers: UInt,
	val available_memory_in_mib: UInt
)

@Serializable
data class LaunchSessionRequest (
	val position_hash: HashKey,
	val nodes_polling_interval_in_ms: UInt? = null
)

/// Generated type representing the anonymous struct variant `Play` of the `Command` Rust enum
@Serializable
data class CommandPlayInner (
	val hash: HashKey,
	val pos: MaybePos,
	val draw_condition: UInt? = null
)

/// Generated type representing the anonymous struct variant `Set` of the `Command` Rust enum
@Serializable
data class CommandSetInner (
	val hash: HashKey,
	val pos: Pos,
	val color: Color
)

/// Generated type representing the anonymous struct variant `Unset` of the `Command` Rust enum
@Serializable
data class CommandUnsetInner (
	val hash: HashKey,
	val pos: Pos,
	val color: Color
)

/// Generated type representing the anonymous struct variant `Undo` of the `Command` Rust enum
@Serializable
data class CommandUndoInner (
	val hash: HashKey
)

@Serializable
sealed class Command {
	@Serializable
	@SerialName("Clear")
	object Clear: Command()
	@Serializable
	@SerialName("Init")
	data class Init(val content: GameStateData): Command()
	@Serializable
	@SerialName("Sync")
	data class Sync(val content: GameStateData): Command()
	@Serializable
	@SerialName("Play")
	data class Play(val content: CommandPlayInner): Command()
	@Serializable
	@SerialName("Set")
	data class Set(val content: CommandSetInner): Command()
	@Serializable
	@SerialName("Unset")
	data class Unset(val content: CommandUnsetInner): Command()
	@Serializable
	@SerialName("Undo")
	data class Undo(val content: CommandUndoInner): Command()
	@Serializable
	@SerialName("RebuildTT")
	data class RebuildTT(val content: ByteSize): Command()
}

/// Generated type representing the anonymous struct variant `Status` of the `Response` Rust enum
@Serializable
data class ResponseStatusInner (
	val hash: HashKey,
	val best_move: MaybePos,
	val score: Score,
	val selective_depth: Depth,
	val total_nodes: Nodes,
	val pv: PrincipalVariation,
	val time_elapsed: Duration
)

@Serializable
sealed class Response {
	@Serializable
	@SerialName("Begins")
	data class Begins(val content: ComputingResource): Response()
	@Serializable
	@SerialName("Status")
	data class Status(val content: ResponseStatusInner): Response()
}

